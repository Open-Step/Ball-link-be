package com.openstep.balllinkbe.features.score.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openstep.balllinkbe.domain.game.GameEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameCommandService {

    private final GameEventWriter eventWriter;
    private final StatAggregator statAggregator;
    private final StateBuilder stateBuilder;  // ✅ 누락된 부분 추가
    private final SimpMessagingTemplate messaging;
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * WebSocket 명령 처리 (핵심 로직)
     */
    @Transactional
    public GameResult handleCommand(Long gameId, Map<String, Object> message) {
        String action = (String) message.get("action");
        Map<String, Object> data = castToMap(message.get("data"));

        log.info("🎮 Handling action: {} for game {}", action, gameId);

        GameResult result = new GameResult();

        switch (action) {

            /** ✅ 세션 입장: 전체 상태 반환 */
            case "session.join" -> {
                Map<String, Object> state = stateBuilder.buildSyncState(gameId);
                result.setStateSync(state);
                return result;
            }

            /** ⏱ 시계 업데이트 */
            case "clock.update" -> {
                eventWriter.recordClock(gameId, data);
                broadcastClockSync(gameId, data);
            }

            /** 🕒 샷클락 리셋 (24초) */
            case "shotclock.reset" -> {
                eventWriter.recordShotclockReset(gameId, data);
                broadcastClockSync(gameId, data);
            }

            /** 🕒 샷클락 리셋 (14초) */
            case "shotclock.reset14" -> {
                eventWriter.recordShotclockReset14(gameId, data);
                broadcastClockSync(gameId, data);
            }

            /** 🏀 득점 추가 */
            case "score.add" -> {
                var ev = eventWriter.recordScore(gameId, data);
                statAggregator.applyScore(gameId, data);
                broadcastPbp(gameId, ev);
            }

            /** 🚫 파울 추가 */
            case "foul.add" -> {
                var ev = eventWriter.recordFoul(gameId, data);
                statAggregator.applyFoul(gameId, data);
                broadcastPbp(gameId, ev);
            }

            /** 🔁 소유권 변경 */
            case "possession.change" -> {
                var ev = eventWriter.recordPossession(gameId, data);
                broadcastPbp(gameId, ev);
            }

            /** 🔄 교체 */
            case "substitution" -> {
                var ev = eventWriter.recordSubstitution(gameId, data);
                broadcastPbp(gameId, ev);
            }

            /** 🕐 타임아웃 */
            case "timeout.request" -> {
                var ev = eventWriter.recordTimeout(gameId, data);
                broadcastPbp(gameId, ev);
            }

            /** 🏁 쿼터 시작 */
            case "period.start" -> {
                var ev = eventWriter.recordPeriodStart(gameId, data);
                broadcastPbp(gameId, ev);
            }

            /** ⛔ 쿼터 종료 */
            case "period.end" -> {
                var ev = eventWriter.recordPeriodEnd(gameId, data);
                broadcastPbp(gameId, ev);
            }

            /** 🗒 메모 추가 */
            case "note.add" -> {
                var ev = eventWriter.recordNote(gameId, data);
                broadcastPbp(gameId, ev);
            }

            /** 🏁 경기 종료 */
            case "game.finish" -> {
                var ev = eventWriter.recordGameFinish(gameId, data);
                statAggregator.finalizeGame(gameId);
                broadcastPbp(gameId, ev);
            }

            default -> log.warn("⚠️ Unknown action: {}", action);
        }

        return result;
    }

    /* ===================== helpers ===================== */

    /** ✅ 안전한 Map 변환 */
    private Map<String, Object> castToMap(Object obj) {
        if (obj == null) return Map.of();
        if (obj instanceof Map<?, ?> m) {
            //noinspection unchecked
            return (Map<String, Object>) m;
        }
        return mapper.convertValue(obj, Map.class);
    }

    /** ✅ 플레이로그(pbp) 브로드캐스트 */
    private void broadcastPbp(Long gameId, GameEvent ev) {
        if (ev == null) return;
        var payload = eventWriter.toPbpEvent(ev); // {type:'event', action:'pbp.append', data:{...}}
        messaging.convertAndSend("/topic/games." + gameId + ".public", payload);
        log.debug("📢 pbp.append broadcasted: {}", payload);
    }

    /** ✅ 클락 동기화 브로드캐스트 (state.clock) */
    private void broadcastClockSync(Long gameId, Map<String, Object> data) {
        Map<String, Object> clock = Map.of(
                "type", "state",
                "action", "clock.sync",
                "data", Map.of(
                        "running", data.getOrDefault("running", false),
                        "timeRemaining", data.getOrDefault("timeRemaining", null),
                        "shotRemaining", data.getOrDefault("shotRemaining", null)
                )
        );
        messaging.convertAndSend("/topic/games." + gameId + ".public", clock);
        log.debug("⏱ clock.sync broadcasted: {}", clock);
    }
}
