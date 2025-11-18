package com.openstep.balllinkbe.features.score.service;

import com.openstep.balllinkbe.domain.game.Game;
import com.openstep.balllinkbe.domain.game.GamePlayerStat;
import com.openstep.balllinkbe.features.score.repository.GamePlayerStatScoreRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class LineupTrackerService {

    private final EntityManager em;
    private final GamePlayerStatScoreRepository statRepo; // 변경됨

    private final Map<Long, Map<Long, LocalDateTime>> onCourtMap = new HashMap<>();
    private final Map<Long, Map<Long, Duration>> totalPlayMap = new HashMap<>();

    /** 교체 발생 시 반영 */
    public void updateLineup(Long gameId, Map<String, Object> data) {
        Long outId = data.get("outPlayerId") != null ? ((Number) data.get("outPlayerId")).longValue() : null;
        Long inId  = data.get("inPlayerId")  != null ? ((Number) data.get("inPlayerId")).longValue()  : null;

        var onCourt = onCourtMap.computeIfAbsent(gameId, k -> new HashMap<>());
        var totals  = totalPlayMap.computeIfAbsent(gameId, k -> new HashMap<>());

        LocalDateTime now = LocalDateTime.now();

        if (outId != null && onCourt.containsKey(outId)) {
            Duration played = Duration.between(onCourt.get(outId), now);
            totals.put(outId, totals.getOrDefault(outId, Duration.ZERO).plus(played));
            onCourt.remove(outId);
            log.info("⛹️‍♂️ OUT: player {} played {}s", outId, played.getSeconds());
        }

        if (inId != null) {
            onCourt.put(inId, now);
            log.info("🏀 IN: player {} joined at {}", inId, now);
        }
    }

    /** 쿼터 시작 시 초기화 */
    public void onPeriodStart(Long gameId, Map<String, Object> data) {
        onCourtMap.putIfAbsent(gameId, new HashMap<>());
        log.info("[Lineup] Period started for game {}", gameId);
    }

    /** 쿼터 종료 시 시간 누적 */
    public void onPeriodEnd(Long gameId, Map<String, Object> data) {
        LocalDateTime now = LocalDateTime.now();
        var onCourt = onCourtMap.getOrDefault(gameId, Map.of());
        var totals  = totalPlayMap.computeIfAbsent(gameId, k -> new HashMap<>());

        onCourt.forEach((pid, inTs) -> {
            Duration played = Duration.between(inTs, now);
            totals.put(pid, totals.getOrDefault(pid, Duration.ZERO).plus(played));
        });
        log.info("[Lineup] Period ended: added {} players’ playtime", onCourt.size());
    }

    /** 경기 종료 시 모두 반영 */
    @Transactional
    public void finalizeLineups(Long gameId) {
        onPeriodEnd(gameId, Map.of());
        var totals = totalPlayMap.getOrDefault(gameId, Map.of());

        Game game = em.find(Game.class, gameId);
        if (game == null) return;

        totals.forEach((playerId, duration) -> {
            double minutes = duration.toSeconds() / 60.0;

            statRepo.findByGameAndPlayerId(game, playerId).ifPresent(stat -> {
                stat.setMinutes(BigDecimal.valueOf(minutes));
                statRepo.save(stat);
                log.info("📊 Player {} total playtime = {} min", playerId, minutes);
            });
        });

        log.info("📊 Finalized lineup minutes saved for {} players", totals.size());
        onCourtMap.remove(gameId);
        totalPlayMap.remove(gameId);
    }
}
