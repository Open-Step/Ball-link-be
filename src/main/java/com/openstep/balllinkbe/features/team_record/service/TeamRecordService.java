package com.openstep.balllinkbe.features.team_record.service;

import com.openstep.balllinkbe.domain.game.GameTeamStat;
import com.openstep.balllinkbe.features.team_record.dto.response.*;
import com.openstep.balllinkbe.features.team_record.repository.TeamRecordRepository;
import com.openstep.balllinkbe.features.team_record.repository.projection.PlayerAggregateProjection;
import com.openstep.balllinkbe.features.team_record.repository.projection.PlayerGameProjection;
import com.openstep.balllinkbe.features.team_record.repository.projection.TournamentAggProjection;
import com.openstep.balllinkbe.global.exception.CustomException;
import com.openstep.balllinkbe.global.exception.ErrorCode;
import com.openstep.balllinkbe.global.util.PageableUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamRecordService {

    private final TeamRecordRepository teamRecordRepository;

    private double avg(int sum, int games) {
        if (games <= 0) return 0.0;
        return BigDecimal.valueOf((double) sum / games)
                .setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    /** 1) 팀 대회참가기록 */
    public TeamTournamentRecordResponse getTournamentRecord(Long tournamentId, Long teamId, String aggregate) {
        List<GameTeamStat> stats = teamRecordRepository.findByTournamentIdAndTeamId(tournamentId, teamId);
        if (stats.isEmpty()) throw new CustomException(ErrorCode.RECORD_NOT_FOUND);

        int games = stats.size();
        int pts = stats.stream().mapToInt(GameTeamStat::getPts).sum();
        int reb = stats.stream().mapToInt(GameTeamStat::getReb).sum();
        int ast = stats.stream().mapToInt(GameTeamStat::getAst).sum();
        int stl = stats.stream().mapToInt(GameTeamStat::getStl).sum();
        int blk = stats.stream().mapToInt(GameTeamStat::getBlk).sum();

        Integer wins = teamRecordRepository.countWinsInTournament(tournamentId, teamId);
        Integer losses = teamRecordRepository.countLossesInTournament(tournamentId, teamId);
        int w = wins == null ? 0 : wins;
        int l = losses == null ? 0 : losses;

        var totals = new TeamTournamentRecordResponse.Stats(pts, reb, ast, stl, blk);
        var perGame = new TeamTournamentRecordResponse.StatsAvg(
                avg(pts, games), avg(reb, games), avg(ast, games), avg(stl, games), avg(blk, games)
        );

        Long tid = stats.get(0).getGame().getTournament() != null ? stats.get(0).getGame().getTournament().getId() : null;
        return TeamTournamentRecordResponse.builder()
                .tournamentId(tid)
                .teamId(teamId)
                .games(games)
                .wins(w)
                .losses(l)
                .totals(totals)
                .perGame(perGame)
                .build();
    }

    /** 2) 팀 통산기록 (시즌/전체) */
    public TeamSeasonRecordResponse getSeasonRecord(Long teamId, String season, String split) {
        List<GameTeamStat> stats = teamRecordRepository.findByTeamIdAndSeason(teamId, season);
        if (stats.isEmpty()) throw new CustomException(ErrorCode.RECORD_NOT_FOUND);

        int games = stats.size();
        int pts = stats.stream().mapToInt(GameTeamStat::getPts).sum();
        int reb = stats.stream().mapToInt(GameTeamStat::getReb).sum();
        int ast = stats.stream().mapToInt(GameTeamStat::getAst).sum();
        int stl = stats.stream().mapToInt(GameTeamStat::getStl).sum();
        int blk = stats.stream().mapToInt(GameTeamStat::getBlk).sum();

        Integer wins = teamRecordRepository.countWinsInSeason(teamId, season);
        Integer losses = teamRecordRepository.countLossesInSeason(teamId, season);
        int w = wins == null ? 0 : wins;
        int l = losses == null ? 0 : losses;

        var totals = new TeamSeasonRecordResponse.Stats(pts, reb, ast, stl, blk);
        var perGame = new TeamSeasonRecordResponse.StatsAvg(
                avg(pts, games), avg(reb, games), avg(ast, games), avg(stl, games), avg(blk, games)
        );

        return TeamSeasonRecordResponse.builder()
                .teamId(teamId)
                .season(season)
                .games(games)
                .wins(w)
                .losses(l)
                .totals(totals)
                .perGame(perGame)
                .build();
    }

    /** 3) 선수 통산기록 (팀단위) */
    public PlayerStatsResponse getPlayerStats(Long teamId, String season, String rankBy) {
        List<PlayerAggregateProjection> rows = teamRecordRepository.aggregatePlayerStats(teamId, season);
        if (rows.isEmpty()) throw new CustomException(ErrorCode.RECORD_NOT_FOUND);

        // 정렬 (rankBy)
        Comparator<PlayerAggregateProjection> cmp = switch (rankBy.toLowerCase()) {
            case "reb" -> Comparator.comparingInt(PlayerAggregateProjection::getReb);
            case "ast" -> Comparator.comparingInt(PlayerAggregateProjection::getAst);
            case "stl" -> Comparator.comparingInt(PlayerAggregateProjection::getStl);
            case "blk" -> Comparator.comparingInt(PlayerAggregateProjection::getBlk);
            default -> Comparator.comparingInt(PlayerAggregateProjection::getPts);
        };
        rows = rows.stream().sorted(cmp.reversed()).toList();

        return PlayerStatsResponse.fromAggregates(rows, rankBy);
    }

    /** 4) 선수 경기별 기록 (페이징) */
    public Page<PlayerGameRecordResponse.Item> getPlayerGameRecords(Long teamId, Long playerId, String season, int page, int size, String sort) {
        var pageable = PageableUtil.from(page, size, sort, "date");
        var pageData = teamRecordRepository.findPlayerGames(teamId, playerId, season, pageable);
        return pageData.map(PlayerGameRecordResponse.Item::from);
    }

    /** 5) 팀 대회목록 (참여 요약) */
    public TournamentSummaryResponse getTeamTournaments(Long teamId, String season) {
        List<TournamentAggProjection> rows = teamRecordRepository.findTournamentSummaries(teamId, season);
        if (rows.isEmpty()) return new TournamentSummaryResponse(List.of());
        return TournamentSummaryResponse.from(rows);
    }

    /** 6) 대회 내 경기목록 */
    public Page<TournamentGameListResponse.Item> getTournamentGames(Long teamId, Long tournamentId, String status,
                                                                    int page, int size, String sort) {
        var pageable = PageableUtil.from(page, size, sort, "date");
        var p = teamRecordRepository.findTournamentGames(teamId, tournamentId, status, pageable);
        return p.map(TournamentGameListResponse.Item::from);
    }

    /** 7) 단일 경기 박스스코어 */
    public GameBoxscoreResponse getGameBoxscore(Long gameId) {
        var head = teamRecordRepository.getGameHeader(gameId)
                .orElseThrow(() -> new CustomException(ErrorCode.RECORD_NOT_FOUND));

        var teamStats = teamRecordRepository.findTeamStatsByGame(gameId);
        if (teamStats.isEmpty()) throw new CustomException(ErrorCode.RECORD_NOT_FOUND);

        // 홈/어웨이 팀 매핑
        var homeTotals = teamStats.stream().filter(s -> s.getTeam() != null &&
                        s.getTeam().getId() != null && s.getTeam().getId().equals(head.getHomeTeamId()))
                .findFirst().orElse(null);
        var awayTotals = teamStats.stream().filter(s -> s.getTeam() != null &&
                        s.getTeam().getId() != null && s.getTeam().getId().equals(head.getAwayTeamId()))
                .findFirst().orElse(null);

        // 선수 라인업
        var lines = teamRecordRepository.findPlayerLines(gameId);
        var homePlayers = lines.stream()
                .filter(l -> head.getHomeTeamId() != null && head.getHomeTeamId().equals(l.getTeamId()))
                .map(GameBoxscoreResponse.PlayerLine::from)
                .toList();
        var awayPlayers = lines.stream()
                .filter(l -> head.getAwayTeamId() != null && head.getAwayTeamId().equals(l.getTeamId()))
                .map(GameBoxscoreResponse.PlayerLine::from)
                .toList();

        var homeBox = GameBoxscoreResponse.TeamBox.from(head.getHomeTeamId(), head.getHomeTeamName(), homeTotals, homePlayers);
        var awayBox = GameBoxscoreResponse.TeamBox.from(head.getAwayTeamId(), head.getAwayTeamName(), awayTotals, awayPlayers);

        return GameBoxscoreResponse.builder()
                .gameId(head.getGameId())
                .tournamentId(head.getTournamentId())
                .tournamentName(head.getTournamentName())
                .date(head.getDate() != null ? head.getDate().toLocalDateTime() : null)
                .venueName(head.getVenueName())
                .home(homeBox)
                .away(awayBox)
                .build();

    }
}
