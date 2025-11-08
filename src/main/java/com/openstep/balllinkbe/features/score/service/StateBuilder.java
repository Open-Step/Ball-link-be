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

    /** ✅ 경기 전체 상태 스냅샷 생성 */
    public Map<String, Object> buildSyncState(Long gameId) {
        Game game = em.find(Game.class, gameId);
        if (game == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("type", "error");
            err.put("message", "Game not found");
            return err;
        }

        Team home = game.getHomeTeam();
        Team away = game.getAwayTeam();

        // 🏀 팀 스탯
        List<GameTeamStat> teamStats = teamRepo.findAll().stream()
                .filter(t -> Objects.equals(t.getGame().getId(), gameId))
                .toList();

        Map<String, Object> homeStat = toTeamStat(teamStats, home.getId());
        Map<String, Object> awayStat = toTeamStat(teamStats, away.getId());

        // 👥 선수별 스탯
        List<GamePlayerStat> playerStats = playerRepo.findAll().stream()
                .filter(p -> Objects.equals(p.getGame().getId(), gameId))
                .toList();

        List<Map<String, Object>> homePlayers = playerStats.stream()
                .filter(p -> p.getTeam().getId().equals(home.getId()))
                .map(this::toPlayerStat)
                .collect(Collectors.toList());

        List<Map<String, Object>> awayPlayers = playerStats.stream()
                .filter(p -> p.getTeam().getId().equals(away.getId()))
                .map(this::toPlayerStat)
                .collect(Collectors.toList());

        // ⏱ 최근 클락 이벤트
        Map<String, Object> clock = getLatestClock(gameId);

        // 📊 쿼터별 점수
        Map<Integer, Integer> quarterScores = getQuarterScores(gameId);

        // 🧩 최종 구조 (Null 안전 HashMap)
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
        data.put("period", game.getStartedAt() != null ? getCurrentPeriod(gameId) : 0);
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

    /* ====== Helper Methods ====== */

    /** ✅ 팀 스탯 변환 (Null 안전) */
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

    /** ✅ 선수 스탯 변환 (Null 안전) */
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

    /** ✅ 최근 클락 이벤트 조회 (Null 안전) */
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

    /** ✅ 쿼터별 점수 */
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
            log.warn("⚠️ Quarter score query failed: {}", e.getMessage());
            Map<Integer, Integer> fallback = new LinkedHashMap<>();
            fallback.put(1, 0); fallback.put(2, 0); fallback.put(3, 0); fallback.put(4, 0);
            return fallback;
        }
    }

    /** ✅ 현재 쿼터 계산 */
    private int getCurrentPeriod(Long gameId) {
        try {
            return eventRepo.findLastStartedPeriod(gameId)
                    .map(GameEvent::getPeriod)
                    .orElse(1);
        } catch (Exception e) {
            log.warn("⚠️ getCurrentPeriod failed: {}", e.getMessage());
            return 1;
        }
    }
}
