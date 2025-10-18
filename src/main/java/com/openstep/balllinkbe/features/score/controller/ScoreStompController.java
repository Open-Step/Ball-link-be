package com.openstep.balllinkbe.features.score.controller;

import com.openstep.balllinkbe.features.score.service.GameCommandService;
import com.openstep.balllinkbe.features.score.service.GameResult;
import com.openstep.balllinkbe.global.config.websocket.IdempotencyCache;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ScoreStompController {

    private final SimpMessagingTemplate messaging;
    private final GameCommandService commandService;
    private final IdempotencyCache idempotencyCache;

    /**
     * WebSocket Entry Point
     * 클라이언트 → /app/games.{gameId}.cmd
     */
    @MessageMapping("/games.{gameId}.cmd")
    public void handleCommand(
            @DestinationVariable Long gameId,
            @Payload Map<String, Object> message,
            Principal principal,
            MessageHeaders headers
    ) {

        Map<String, Object> meta = (Map<String, Object>) message.get("meta");
        String action = (String) message.get("action");
        String idempotencyKey = meta != null ? (String) meta.get("idempotencyKey") : null;
        String idemKey = gameId + ":" + (idempotencyKey == null ? "noid" : idempotencyKey);

        // 멱등성 캐시 검사
        if (idempotencyKey != null && idempotencyCache.seen(idemKey)) {
            idempotencyCache.get(idemKey).ifPresent(cached ->
                    messaging.convertAndSendToUser(
                            principal.getName(),
                            "/queue/games." + gameId + ".ack",
                            cached
                    )
            );
            return;
        }

        try {
            // 실제 명령 처리 (DB Insert + 통계 반영 + 내부 broadcast)
            GameResult result = commandService.handleCommand(gameId, message);

            // ACK 전송
            Map<String, Object> ack = Map.of(
                    "type", "ack",
                    "action", action,
                    "meta", meta,
                    "data", Map.of("ok", true)
            );

            messaging.convertAndSendToUser(
                    principal.getName(),
                    "/queue/games." + gameId + ".ack",
                    ack
            );

            // 멱등성 캐시에 ACK 저장
            if (idempotencyKey != null) idempotencyCache.put(idemKey, ack);

            // ① 실시간 이벤트들 전송 (pbp.append)
            if (result.getEvents() != null && !result.getEvents().isEmpty()) {
                result.getEvents().forEach(evt ->
                        messaging.convertAndSend("/topic/games." + gameId + ".public", evt)
                );
            }

            // ② 전체 상태(state.sync) 전송
            if (result.getStateSync() != null) {
                messaging.convertAndSend("/topic/games." + gameId + ".public", result.getStateSync());
            }

        } catch (Exception e) {
            // 예외 발생 시 에러 응답
            Map<String, Object> err = Map.of(
                    "type", "error",
                    "action", action,
                    "data", Map.of("code", 400, "message", e.getMessage()),
                    "meta", meta
            );

            messaging.convertAndSendToUser(
                    principal.getName(),
                    "/queue/games." + gameId + ".ack",
                    err
            );
        }
    }
}
