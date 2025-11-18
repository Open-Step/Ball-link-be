package com.openstep.balllinkbe.features.score.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openstep.balllinkbe.domain.game.Game;
import com.openstep.balllinkbe.domain.game.GameEvent;
import com.openstep.balllinkbe.features.game.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameCommandService {

    private final GameEventWriter eventWriter;
    private final StatAggregator statAggregator;
    private final StateBuilder stateBuilder;
    private final LineupTrackerService lineupTracker;
    private final PlayerResolver playerResolver;
    private final SimpMessagingTemplate messaging;
    private final GameRepository gameRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    @Transactional
    public GameResult handleCommand(Long gameId, Map<String, Object> message) {
        String action = (String) message.get("action");
        Map<String, Object> raw = castToMap(message.get("data"));

        // 등번호 기반 playerId 변환
        Map<String, Object> data = switch (action) {
            case "score.add", "foul.add", "rebound.add" -> playerResolver.enrichWithPlayerIds(gameId, raw);
            case "substitution" -> playerResolver.enrichSubstitution(gameId, raw);
            default -> raw;
        };

        log.info("🎮 Handling action: {} for game {}", action, gameId);
        GameResult result = new GameResult();

        switch (action) {

            case "session.join" -> {
                Map<String, Object> state = stateBuilder.buildSyncState(gameId);
                result.setStateSync(state);
                return result;
            }

            case "clock.update" -> {
                eventWriter.recordClock(gameId, data);
                broadcastClockSync(gameId, data);
            }

            case "shotclock.reset" -> {
                eventWriter.recordShotclockReset(gameId, data);
                broadcastClockSync(gameId, data);
            }

            case "shotclock.reset14" -> {
                eventWriter.recordShotclockReset14(gameId, data);
                broadcastClockSync(gameId, data);
            }

            case "score.add" -> {
                var ev = eventWriter.recordScore(gameId, data);
                statAggregator.applyScore(gameId, data);
                broadcastPbp(gameId, ev);
            }

            case "foul.add" -> {
                var ev = eventWriter.recordFoul(gameId, data);
                statAggregator.applyFoul(gameId, data);
                broadcastPbp(gameId, ev);
            }

            case "rebound.add" -> {
                var ev = eventWriter.recordRebound(gameId, data);
                statAggregator.applyRebound(gameId, data);
                broadcastPbp(gameId, ev);
            }

            case "substitution" -> {
                var ev = eventWriter.recordSubstitution(gameId, data);
                lineupTracker.updateLineup(gameId, data);
                broadcastPbp(gameId, ev);
            }

            case "period.start" -> {
                var ev = eventWriter.recordPeriodStart(gameId, data);
                lineupTracker.onPeriodStart(gameId, data);
                broadcastPbp(gameId, ev);
            }

            case "period.end" -> {
                var ev = eventWriter.recordPeriodEnd(gameId, data);
                lineupTracker.onPeriodEnd(gameId, data);
                broadcastPbp(gameId, ev);
            }

            case "timeout.request" -> {
                var ev = eventWriter.recordTimeout(gameId, data);
                broadcastPbp(gameId, ev);
            }

            case "game.finish" -> {
                var ev = eventWriter.recordGameFinish(gameId, data);
                lineupTracker.finalizeLineups(gameId);
                statAggregator.finalizeGame(gameId);
                broadcastPbp(gameId, ev);
            }

            default -> log.warn("⚠Unknown action: {}", action);
        }
        return result;
    }

    private Map<String, Object> castToMap(Object obj) {
        if (obj == null) return Map.of();
        if (obj instanceof Map<?, ?> m) return (Map<String, Object>) m;
        return mapper.convertValue(obj, Map.class);
    }

    /**
     * gameId 기준으로 topic 경로 결정
     *  - scrimmage: /topic/scrimmages.{gameId}.public
     *  - 일반 경기: /topic/games.{gameId}.public
     */
    private String topic(Long gameId) {
        Game game = gameRepository.findById(gameId).orElse(null);
        if (game != null && game.isScrimmage()) {
            return "/topic/scrimmages." + gameId + ".public";
        }
        return "/topic/games." + gameId + ".public";
    }

    private void broadcastPbp(Long gameId, GameEvent ev) {
        if (ev == null) return;
        var payload = eventWriter.toPbpEvent(ev);
        messaging.convertAndSend(topic(gameId), payload);
        log.debug("pbp.append broadcasted to {}: {}", topic(gameId), payload);
    }

    private void broadcastClockSync(Long gameId, Map<String, Object> data) {
        Map<String, Object> wrapper = new HashMap<>();
        wrapper.put("type", "state");
        wrapper.put("action", "clock.sync");

        Map<String, Object> clock = new HashMap<>();
        clock.put("running", data.getOrDefault("running", false));
        clock.put("timeRemaining", data.getOrDefault("timeRemaining", null));
        clock.put("shotRemaining", data.getOrDefault("shotRemaining", null));

        wrapper.put("data", clock);
        messaging.convertAndSend(topic(gameId), wrapper);
        log.debug("clock.sync broadcasted to {}: {}", topic(gameId), wrapper);
    }
}
