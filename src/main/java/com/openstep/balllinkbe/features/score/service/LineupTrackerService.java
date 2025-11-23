package com.openstep.balllinkbe.features.score.service;

import com.openstep.balllinkbe.domain.game.Game;
import com.openstep.balllinkbe.features.score.repository.GamePlayerStatScoreRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class LineupTrackerService {

    private final EntityManager em;
    private final GamePlayerStatScoreRepository statRepo;

    // gameId -> (playerId -> 코트에 들어온 시각)
    private final Map<Long, Map<Long, LocalDateTime>> onCourtMap = new HashMap<>();
    // gameId -> (playerId -> 누적 초)
    private final Map<Long, Map<Long, Long>> totalSecondsMap = new HashMap<>();

    /**
     * 선수 교체 이벤트 처리
     * data: { outPlayerId / playerOut / out,  inPlayerId / playerIn / in }
     */
    public void updateLineup(Long gameId, Map<String, Object> data, LocalDateTime eventTs) {
        Long outId = extractPlayerId(data, "outPlayerId", "playerOut", "out");
        Long inId  = extractPlayerId(data, "inPlayerId",  "playerIn",  "in");

        var onCourt = onCourtMap.computeIfAbsent(gameId, k -> new HashMap<>());
        var totals  = totalSecondsMap.computeIfAbsent(gameId, k -> new HashMap<>());

        // 나가는 선수 시간 누적
        if (outId != null && onCourt.containsKey(outId)) {
            LocalDateTime enterAt = onCourt.remove(outId);
            long seconds = Duration.between(enterAt, eventTs).getSeconds();
            if (seconds > 0) {
                totals.merge(outId, seconds, Long::sum);
            }
            log.info("⛹️ OUT player {} +{}s (total {}s)", outId, seconds, totals.get(outId));
        }

        // 들어오는 선수 입장 시각 기록
        if (inId != null) {
            onCourt.put(inId, eventTs);
            log.info("🏀 IN player {} at {}", inId, eventTs);
        }
    }

    /**
     * 쿼터 시작 이벤트 (지금은 로깅만, 필요하면 나중에 확장)
     */
    public void onPeriodStart(Long gameId, Map<String, Object> data, LocalDateTime eventTs) {
        log.info("[Lineup] period.start game={}, ts={}", gameId, eventTs);

        var onCourt = onCourtMap.computeIfAbsent(gameId, k -> new HashMap<>());
        totalSecondsMap.computeIfAbsent(gameId, k -> new HashMap<>());

        // 스타팅 멤버 조회 (GameLineupPlayer 엔티티 기준)
        var lineup = em.createQuery(
                        "select lp.player.id from GameLineupPlayer lp " +
                                "where lp.game.id = :gid and lp.isStarter = true",  // <<< 핵심 수정
                        Long.class
                )
                .setParameter("gid", gameId)
                .getResultList();

        for (Long playerId : lineup) {
            if (!onCourt.containsKey(playerId)) {
                onCourt.put(playerId, eventTs);
                log.info("PeriodStart → player {} marked IN at {}", playerId, eventTs);
            }
        }
    }


    /**
     * 쿼터 종료 이벤트 (지금은 로깅만, 필요하면 쿼터별 시간도 나눌 수 있음)
     */
    public void onPeriodEnd(Long gameId, Map<String, Object> data, LocalDateTime eventTs) {
        log.info("[Lineup] period.end game={}, ts={}", gameId, eventTs);
    }

    /**
     * 경기 종료 시 남아 있는 선수 시간까지 모두 반영하고 DB에 minutes 저장
     */
    @Transactional
    public void finalizeLineups(Long gameId, LocalDateTime finishTs) {
        var onCourt = onCourtMap.getOrDefault(gameId, Map.of());
        var totals  = totalSecondsMap.computeIfAbsent(gameId, k -> new HashMap<>());

        // 아직 코트에 있는 선수들 시간 정산
        onCourt.forEach((playerId, enterAt) -> {
            long seconds = Duration.between(enterAt, finishTs).getSeconds();
            if (seconds > 0) {
                totals.merge(playerId, seconds, Long::sum);
            }
        });

        Game game = em.find(Game.class, gameId);
        if (game == null) return;

        // game_player_stats.minutes 업데이트
        totals.forEach((playerId, totalSeconds) -> {
            double minutes = totalSeconds / 60.0;
            statRepo.findByGameAndPlayerId(game, playerId).ifPresent(stat -> {
                stat.setMinutes(BigDecimal.valueOf(minutes));
                statRepo.save(stat);
            });
            log.info("📊 player {} total playtime {}s ({} min)", playerId, totalSeconds, minutes);
        });

        // 메모리 정리
        onCourtMap.remove(gameId);
        totalSecondsMap.remove(gameId);
        log.info("📊 Finalized lineup minutes for game {}", gameId);
    }

    private Long extractPlayerId(Map<String, Object> data, String... keys) {
        for (String key : keys) {
            Object v = data.get(key);
            if (v instanceof Number n) {
                return n.longValue();
            }
        }
        return null;
    }
}
