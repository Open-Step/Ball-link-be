package com.openstep.balllinkbe.features.score.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openstep.balllinkbe.domain.game.Game;
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

import static java.util.Map.entry;

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
            log.warn("⚠️ Game not found: {}", gameId);
            return Map.of("type", "error", "message", "Game not found");
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

        // 🧩 최종 구조
        return Map.ofEntries(
                entry("type", "state"),
                entry("action", "state.sync"),
                entry("data", Map.ofEntries(
                        entry("gameId", game.getId()),
                        entry("period", game.getStartedAt() != null ? getCurrentPeriod(gameId) : 0),
                        entry("home", Map.ofEntries(
                                entry("teamId", home.getId()),
                                entry("teamName", home.getName()),
                                entry("stat", homeStat),
                                entry("players", homePlayers)
                        )),
                        entry("away", Map.ofEntries(
                                entry("teamId", away.getId()),
                                entry("teamName", away.getName()),
                                entry("stat", awayStat),
                                entry("players", awayPlayers)
                        )),
                        entry("clock", clock),
                        entry("quarters", quarterScores)
                ))
        );
    }

    /* ====== Helper Methods ====== */

    /** ✅ 팀 스탯 변환 */
    private Map<String, Object> toTeamStat(List<GameTeamStat> stats, Long teamId) {
        return stats.stream()
                .filter(s -> s.getTeam().getId().equals(teamId))
                .findFirst()
                .map(s -> Map.ofEntries(
                        entry("pts", s.getPts()),
                        entry("reb", s.getReb()),
                        entry("ast", s.getAst()),
                        entry("pf", s.getPf()),
                        entry("stl", s.getStl()),
                        entry("blk", s.getBlk()),
                        entry("tov", s.getTov())
                ))
                .map(m -> (Map<String, Object>)(Map<?, ?>) m) // ✅ 추가된 부분
                .orElseGet(Map::of);
    }


    /** ✅ 선수 스탯 변환 */
    private Map<String, Object> toPlayerStat(GamePlayerStat p) {
        return Map.ofEntries(
                entry("playerId", p.getPlayer().getId()),
                entry("name", p.getPlayer().getName()),
                entry("number", p.getPlayer().getNumber()),
                entry("position", p.getPlayer().getPosition() != null ? p.getPlayer().getPosition().name() : null),
                entry("pts", p.getPts()),
                entry("reb", p.getReb()),
                entry("ast", p.getAst()),
                entry("stl", p.getStl()),
                entry("blk", p.getBlk()),
                entry("pf", p.getPf()),
                entry("tov", p.getTov())
        );
    }

    /** ✅ 최근 클락 이벤트 조회 */
    private Map<String, Object> getLatestClock(Long gameId) {
        return eventRepo.findLatestClockEvent(gameId)
                .map(ev -> Map.<String, Object>of(
                        "running", true,
                        "timeRemaining", ev.getClockTime(),
                        "updatedAt", ev.getTs()
                ))
                .orElseGet(() -> Map.<String, Object>of(
                        "running", false,
                        "timeRemaining", "10:00"
                ));
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
            return Map.of(1, 0, 2, 0, 3, 0, 4, 0);
        }
    }

    /** ✅ 현재 쿼터 계산 */
    private int getCurrentPeriod(Long gameId) {
        try {
            return eventRepo.findLastStartedPeriod(gameId)
                    .map(GameEvent -> GameEvent.getPeriod())
                    .orElse(1);
        } catch (Exception e) {
            log.warn("⚠️ getCurrentPeriod failed: {}", e.getMessage());
            return 1;
        }
    }
}
