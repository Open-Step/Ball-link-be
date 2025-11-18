package com.openstep.balllinkbe.features.score.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openstep.balllinkbe.domain.game.Game;
import com.openstep.balllinkbe.domain.game.GameEvent;
import com.openstep.balllinkbe.domain.game.GamePlayerStat;
import com.openstep.balllinkbe.domain.game.GameTeamStat;
import com.openstep.balllinkbe.domain.team.Team;
import com.openstep.balllinkbe.features.score.repository.GameEventRepository;
import com.openstep.balllinkbe.features.score.repository.GamePlayerStatScoreRepository;
import com.openstep.balllinkbe.features.score.repository.GameTeamStatScoreRepository;
import com.openstep.balllinkbe.features.scrimmage.dto.response.ScrimmageDetailResponse;
import com.openstep.balllinkbe.features.scrimmage.service.ScrimmageService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StateBuilder {

    private final EntityManager em;
    private final GameTeamStatScoreRepository teamRepo;
    private final GamePlayerStatScoreRepository playerRepo;
    private final GameEventRepository eventRepo;
    private final ObjectMapper mapper = new ObjectMapper();

    // 스크리미지 라인업 조회용
    private final ScrimmageService scrimmageService;

    /** 경기 전체 상태 스냅샷 생성 */
    public Map<String, Object> buildSyncState(Long gameId) {
        Game game = em.find(Game.class, gameId);
        if (game == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("type", "error");
            err.put("message", "Game not found");
            return err;
        }

        // 스크리미지면 별도 빌더 사용
        if (game.isScrimmage()) {
            return buildScrimmageState(game);
        }

        // 일반 토너먼트/리그 경기
        Team home = game.getHomeTeam();
        Team away = game.getAwayTeam();

        // 팀 스탯
        List<GameTeamStat> teamStats = teamRepo.findAll().stream()
                .filter(t -> Objects.equals(t.getGame().getId(), game.getId()))
                .toList();

        Map<String, Object> homeStat = toTeamStat(teamStats, home.getId());
        Map<String, Object> awayStat = toTeamStat(teamStats, away.getId());

        // 선수별 스탯
        List<GamePlayerStat> playerStats = playerRepo.findAll().stream()
                .filter(p -> Objects.equals(p.getGame().getId(), game.getId()))
                .toList();

        List<Map<String, Object>> homePlayers = playerStats.stream()
                .filter(p -> p.getTeam().getId().equals(home.getId()))
                .map(this::toPlayerStat)
                .collect(Collectors.toList());

        List<Map<String, Object>> awayPlayers = playerStats.stream()
                .filter(p -> p.getTeam().getId().equals(away.getId()))
                .map(this::toPlayerStat)
                .collect(Collectors.toList());

        // 최근 클락 이벤트
        Map<String, Object> clock = getLatestClock(game.getId());

        // 쿼터별 점수
        Map<Integer, Integer> quarterScores = getQuarterScores(game.getId());

        // 최종 구조
        Map<String, Object> homeBlock = new HashMap<>();
        homeBlock.put("teamId", home.getId());
        homeBlock.put("teamName", home.getName());
        homeBlock.put("stat", homeStat);
        homeBlock.put("players", homePlayers);

        Map<String, Object> awayBlock = new HashMap<>();
        awayBlock.put("teamId", away.getId());
        awayBlock.put("teamName", away.getName());
        awayBlock.put("stat", awayStat);
        awayBlock.put("players", awayPlayers);

        Map<String, Object> data = new HashMap<>();
        data.put("gameId", game.getId());
        data.put("period", game.getStartedAt() != null ? getCurrentPeriod(game.getId()) : 0);
        data.put("home", homeBlock);
        data.put("away", awayBlock);
        data.put("clock", clock);
        data.put("quarters", quarterScores);

        Map<String, Object> out = new HashMap<>();
        out.put("type", "state");
        out.put("action", "state.sync");
        out.put("data", data);

        return out;
    }

    /** 스크리미지 전용 상태 생성 */
    private Map<String, Object> buildScrimmageState(Game game) {
        Long gameId = game.getId();

        // 인메모리 라인업 불러오기
        List<ScrimmageDetailResponse.PlayerLineup> lineup =
                scrimmageService.getLineupRaw(gameId);

        var homePlayers = lineup.stream()
                .filter(p -> "HOME".equalsIgnoreCase(p.getTeamSide()))
                .map(this::toScrimmagePlayer)
                .toList();

        var awayPlayers = lineup.stream()
                .filter(p -> "AWAY".equalsIgnoreCase(p.getTeamSide()))
                .map(this::toScrimmagePlayer)
                .toList();

        // 팀 스탯 = 0 초기화
        Map<String, Object> zeroStat = Map.of(
                "pts", 0,
                "reb", 0,
                "ast", 0,
                "pf", 0,
                "stl", 0,
                "blk", 0,
                "tov", 0
        );

        Map<String, Object> homeBlock = new HashMap<>();
        homeBlock.put("teamId", game.getHomeTeam().getId());
        homeBlock.put("teamName", game.getHomeTeam().getName());
        homeBlock.put("stat", zeroStat);
        homeBlock.put("players", homePlayers);

        Map<String, Object> awayBlock = new HashMap<>();
        awayBlock.put("teamId", game.getAwayTeam().getId());
        awayBlock.put("teamName", game.getAwayTeam().getName());
        homeBlock.put("stat", zeroStat);
        awayBlock.put("stat", zeroStat);
        awayBlock.put("players", awayPlayers);

        Map<String, Object> data = new HashMap<>();
        data.put("gameId", gameId);
        data.put("period", 1); // 스크리미지는 기본 1쿼터부터 시작
        data.put("home", homeBlock);
        data.put("away", awayBlock);
        data.put("clock", Map.of("running", false, "timeRemaining", "10:00"));
        data.put("quarters", Map.of(1, 0, 2, 0, 3, 0, 4, 0));

        Map<String, Object> out = new HashMap<>();
        out.put("type", "state");
        out.put("action", "state.sync");
        out.put("data", data);

        return out;
    }

    /** 스크리미지용 선수 변환 */
    private Map<String, Object> toScrimmagePlayer(ScrimmageDetailResponse.PlayerLineup p) {
        Map<String, Object> m = new HashMap<>();
        m.put("playerId", p.getPlayerId());
        m.put("name", p.getName());
        m.put("number", p.getNumber());
        m.put("position", p.getPosition());
        // 스탯은 일단 0으로 시작, 진행 중에 StatAggregator 가 채워 넣을 수 있음
        m.put("pts", 0);
        m.put("reb", 0);
        m.put("ast", 0);
        m.put("stl", 0);
        m.put("blk", 0);
        m.put("pf", 0);
        m.put("tov", 0);
        return m;
    }

    /** 팀 스탯 변환 (Null 안전) */
    private Map<String, Object> toTeamStat(List<GameTeamStat> stats, Long teamId) {
        return stats.stream()
                .filter(s -> s.getTeam().getId().equals(teamId))
                .findFirst()
                .map(s -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("pts", s.getPts());
                    m.put("reb", s.getReb());
                    m.put("ast", s.getAst());
                    m.put("pf", s.getPf());
                    m.put("stl", s.getStl());
                    m.put("blk", s.getBlk());
                    m.put("tov", s.getTov());
                    return m;
                })
                .orElseGet(HashMap::new);
    }

    /** 선수 스탯 변환 (Null 안전) */
    private Map<String, Object> toPlayerStat(GamePlayerStat p) {
        Map<String, Object> m = new HashMap<>();
        m.put("playerId", p.getPlayer().getId());
        m.put("name", p.getPlayer().getName());
        m.put("number", p.getPlayer().getNumber());
        m.put("position", p.getPlayer().getPosition() != null ? p.getPlayer().getPosition().name() : null);
        m.put("pts", p.getPts());
        m.put("reb", p.getReb());
        m.put("ast", p.getAst());
        m.put("stl", p.getStl());
        m.put("blk", p.getBlk());
        m.put("pf", p.getPf());
        m.put("tov", p.getTov());
        return m;
    }

    /** 최근 클락 이벤트 조회 (Null 안전) */
    private Map<String, Object> getLatestClock(Long gameId) {
        return eventRepo.findLatestClockEvent(gameId)
                .map(ev -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("running", true);
                    m.put("timeRemaining", ev.getClockTime());
                    m.put("updatedAt", ev.getTs());
                    return m;
                })
                .orElseGet(() -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("running", false);
                    m.put("timeRemaining", "10:00");
                    return m;
                });
    }

    /** 쿼터별 점수 */
    private Map<Integer, Integer> getQuarterScores(Long gameId) {
        try {
            List<Map<String, Object>> rows = eventRepo.findQuarterScores(gameId);
            Map<Integer, Integer> out = new LinkedHashMap<>();
            for (Map<String, Object> row : rows) {
                Integer period = ((Number) row.get("period")).intValue();
                Integer pts = ((Number) row.get("pts")).intValue();
                out.put(period, pts);
            }
            return out;
        } catch (Exception e) {
            log.warn("Quarter score query failed: {}", e.getMessage());
            Map<Integer, Integer> fallback = new LinkedHashMap<>();
            fallback.put(1, 0);
            fallback.put(2, 0);
            fallback.put(3, 0);
            fallback.put(4, 0);
            return fallback;
        }
    }

    /** 현재 쿼터 계산 */
    private int getCurrentPeriod(Long gameId) {
        try {
            return eventRepo.findLastStartedPeriod(gameId)
                    .map(GameEvent::getPeriod)
                    .orElse(1);
        } catch (Exception e) {
            log.warn("getCurrentPeriod failed: {}", e.getMessage());
            return 1;
        }
    }
}
