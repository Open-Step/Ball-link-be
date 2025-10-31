package com.openstep.balllinkbe.features.team_record.service;

import com.openstep.balllinkbe.domain.game.Game;
import com.openstep.balllinkbe.domain.game.GameTeamStat;
import com.openstep.balllinkbe.domain.team.Team;
import com.openstep.balllinkbe.domain.tournament.Tournament;
import com.openstep.balllinkbe.domain.venue.Venue;
import com.openstep.balllinkbe.features.game.repository.GameRepository;
import com.openstep.balllinkbe.features.team_record.dto.response.*;
import com.openstep.balllinkbe.features.team_record.repository.TeamRecordRepository;
import com.openstep.balllinkbe.features.team_record.repository.projection.PlayerAggregateProjection;
import com.openstep.balllinkbe.features.team_record.repository.projection.PlayerGameProjection;
import com.openstep.balllinkbe.features.team_record.repository.projection.SeasonAggProjection;
import com.openstep.balllinkbe.features.team_record.repository.projection.TournamentAggProjection;
import com.openstep.balllinkbe.global.exception.CustomException;
import com.openstep.balllinkbe.global.exception.ErrorCode;
import com.openstep.balllinkbe.global.util.PageableUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TeamRecordService {

    private final TeamRecordRepository teamRecordRepository;
    private final GameRepository  gameRepository;
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

    /** 2) 팀 통산기록 (연도별) *//** 팀 통산기록 (연도별) */
    public TeamSeasonRecordResponse getSeasonRecord(Long teamId) {
        List<SeasonAggProjection> rows = teamRecordRepository.findSeasonSummaries(teamId);
        if (rows.isEmpty()) throw new CustomException(ErrorCode.RECORD_NOT_FOUND);

        var items = rows.stream().map(r -> TeamSeasonRecordResponse.Item.builder()
                .season(r.getSeason())
                .games(r.getGames())
                .totals(new TeamSeasonRecordResponse.Stats(
                        r.getPts(), r.getReb(), r.getAst(), r.getStl(), r.getBlk(),
                        r.getFg2(), r.getFg3(), r.getFt()
                ))
                .perGame(new TeamSeasonRecordResponse.StatsAvg(
                        avg(r.getPts(), r.getGames()),
                        avg(r.getReb(), r.getGames()),
                        avg(r.getAst(), r.getGames()),
                        avg(r.getStl(), r.getGames()),
                        avg(r.getBlk(), r.getGames()),
                        avg(r.getFg2(), r.getGames()),
                        avg(r.getFg3(), r.getGames()),
                        avg(r.getFt(), r.getGames())
                ))
                .build()).toList();

        return new TeamSeasonRecordResponse(items);
    }




    /** 3) 선수 통산기록 (팀단위) */
    public PlayerStatsResponse getPlayerStats(Long teamId) {
        List<PlayerAggregateProjection> rows = teamRecordRepository.aggregatePlayerStats(teamId);
        if (rows.isEmpty()) throw new CustomException(ErrorCode.RECORD_NOT_FOUND);

        // 기본 정렬: 득점 순
        rows = rows.stream()
                .sorted(Comparator.comparingInt(PlayerAggregateProjection::getPts).reversed())
                .toList();

        return PlayerStatsResponse.fromAggregates(rows, "pts");
    }


    /** 4) 선수 경기별 기록 (페이징) */
    public Page<PlayerGameRecordResponse.Item> getPlayerGameRecords(Long teamId, Long playerId, String season, int page, int size, String sort) {
        var pageable = PageableUtil.from(page, size, sort, "date");
        var pageData = teamRecordRepository.findPlayerGames(teamId, playerId, season, pageable);
        return pageData.map(PlayerGameRecordResponse.Item::from);
    }

    /** 5) 팀 대회별 누적기록 */
    public TournamentSummaryResponse getTeamTournaments(Long teamId, String season) {
        List<TournamentAggProjection> rows = teamRecordRepository.findTournamentSummaries(teamId, season);
        if (rows.isEmpty()) throw new CustomException(ErrorCode.RECORD_NOT_FOUND);

        var items = rows.stream().map(r -> TournamentSummaryResponse.Item.builder()
                .tournamentId(r.getTournamentId())
                .tournamentName(r.getTournamentName())
                .games(r.getGames())
                .wins(r.getWins())
                .losses(r.getLosses())
                .totals(new TournamentSummaryResponse.Stats(
                        r.getPts(), r.getReb(), r.getAst(), r.getStl(), r.getBlk(),
                        r.getFg2(), r.getFg3(), r.getFt()
                ))
                .perGame(new TournamentSummaryResponse.StatsAvg(
                        avg(r.getPts(), r.getGames()),
                        avg(r.getReb(), r.getGames()),
                        avg(r.getAst(), r.getGames()),
                        avg(r.getStl(), r.getGames()),
                        avg(r.getBlk(), r.getGames()),
                        avg(r.getFg2(), r.getGames()),
                        avg(r.getFg3(), r.getGames()),
                        avg(r.getFt(), r.getGames())
                ))
                .build()).toList();

        return new TournamentSummaryResponse(items);
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
        var homeTotals = teamStats.stream()
                .filter(s -> s.getTeam() != null && Objects.equals(s.getTeam().getId(), head.getHomeTeamId()))
                .findFirst().orElse(null);
        var awayTotals = teamStats.stream()
                .filter(s -> s.getTeam() != null && Objects.equals(s.getTeam().getId(), head.getAwayTeamId()))
                .findFirst().orElse(null);

        // 선수 라인업
        var lines = teamRecordRepository.findPlayerLines(gameId);
        var homePlayers = lines.stream()
                .filter(l -> Objects.equals(head.getHomeTeamId(), l.getTeamId()))
                .map(GameBoxscoreResponse.PlayerLine::from)
                .toList();
        var awayPlayers = lines.stream()
                .filter(l -> Objects.equals(head.getAwayTeamId(), l.getTeamId()))
                .map(GameBoxscoreResponse.PlayerLine::from)
                .toList();

        var homeBox = GameBoxscoreResponse.TeamBox.from(
                head.getHomeTeamId(), head.getHomeTeamName(), homeTotals, homePlayers);
        var awayBox = GameBoxscoreResponse.TeamBox.from(
                head.getAwayTeamId(), head.getAwayTeamName(), awayTotals, awayPlayers);

        // ──────────────────────────────
        // 쿼터별 득점 계산 (PDF와 동일 로직)
        // ──────────────────────────────
        int[] homeQ = new int[]{0, 0, 0, 0, 0};
        int[] awayQ = new int[]{0, 0, 0, 0, 0};
        for (var q : teamRecordRepository.findQuarterScores(gameId)) {
            int idx = Math.min(q.getPeriod(), 5) - 1;
            if (Objects.equals(head.getHomeTeamId(), q.getTeamId())) homeQ[idx] = q.getPts();
            if (Objects.equals(head.getAwayTeamId(), q.getTeamId())) awayQ[idx] = q.getPts();
        }

        Map<Long, int[]> playerQ = new HashMap<>();
        for (var p : teamRecordRepository.findPlayerQuarterScores(gameId)) {
            int idx = Math.min(p.getPeriod(), 5) - 1;
            int[] arr = playerQ.computeIfAbsent(p.getPlayerId(), __ -> new int[]{0, 0, 0, 0, 0});
            arr[idx] = p.getPts();
        }

        // ──────────────────────────────
        // 선수별 쿼터별 득점 추가
        // ──────────────────────────────
        homeBox.getPlayers().forEach(p -> {
            int[] q = playerQ.getOrDefault(p.getPlayerId(), new int[]{0, 0, 0, 0, 0});
            p.setQ1(q[0]);
            p.setQ2(q[1]);
            p.setQ3(q[2]);
            p.setQ4(q[3]);
            p.setOt(q[4]);
        });

        awayBox.getPlayers().forEach(p -> {
            int[] q = playerQ.getOrDefault(p.getPlayerId(), new int[]{0, 0, 0, 0, 0});
            p.setQ1(q[0]);
            p.setQ2(q[1]);
            p.setQ3(q[2]);
            p.setQ4(q[3]);
            p.setOt(q[4]);
        });

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

    @Transactional()
    public GameInfoResponse getGameInfo(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new CustomException(ErrorCode.GAME_NOT_FOUND));

        Team home = game.getHomeTeam();
        Team away = game.getAwayTeam();
        Tournament tournament = game.getTournament();
        Venue venue = game.getVenue();

        return GameInfoResponse.builder()
                .gameId(game.getId())
                .tournamentName(tournament != null ? tournament.getName() : null)
                .scheduledAt(game.getScheduledAt())
                .venueName(venue != null ? venue.getName() : null)
                .homeTeamName(home != null ? home.getName() : null)
                .homeTeamId(home != null ? home.getId() : null)
                .awayTeamName(away != null ? away.getName() : null)
                .awayTeamId(away != null ? away.getId() : null)
                .build();
    }

}
