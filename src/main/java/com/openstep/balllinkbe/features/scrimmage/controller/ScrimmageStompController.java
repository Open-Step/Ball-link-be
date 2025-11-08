package com.openstep.balllinkbe.features.scrimmage.controller;

import com.openstep.balllinkbe.features.score.service.GameCommandService;
import com.openstep.balllinkbe.features.score.service.GameResult;
import com.openstep.balllinkbe.global.config.websocket.IdempotencyCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ScrimmageStompController {

    private final SimpMessagingTemplate messaging;
    private final GameCommandService commandService;
    private final IdempotencyCache idempotencyCache;

    /**
     * 🏀 스크리미지 WebSocket Entry Point
     * 클라이언트 → /app/scrimmages.{gameId}.cmd
     */
    @MessageMapping("/scrimmages.{gameId}.cmd")
    public void handleScrimmageCommand(
            @DestinationVariable Long gameId,
            @Payload Map<String, Object> message,
            Principal principal,
            MessageHeaders headers
    ) {
        Map<String, Object> meta = (Map<String, Object>) message.get("meta");
        String action = (String) message.get("action");
        String idempotencyKey = meta != null ? (String) meta.get("idempotencyKey") : null;
        String idemKey = "scrimmage:" + gameId + ":" + (idempotencyKey == null ? "noid" : idempotencyKey);

        String username = principal != null ? principal.getName() : "anonymous";

        // ✅ 멱등성 검사
        if (idempotencyKey != null && idempotencyCache.seen(idemKey)) {
            idempotencyCache.get(idemKey).ifPresent(cached ->
                    messaging.convertAndSendToUser(
                            username,
                            "/queue/scrimmages." + gameId + ".ack",
                            cached
                    )
            );
            return;
        }

        try {
            // ✅ 실제 명령 처리 (DB Insert + 통계 반영 + 내부 broadcast)
            GameResult result = commandService.handleCommand(gameId, message);

            // ✅ ACK 생성
            Map<String, Object> ack = new HashMap<>();
            ack.put("type", "ack");
            ack.put("action", action);
            ack.put("meta", meta);
            ack.put("data", Map.of("ok", true));

            messaging.convertAndSendToUser(
                    username,
                    "/queue/scrimmages." + gameId + ".ack",
                    ack
            );

            if (idempotencyKey != null) idempotencyCache.put(idemKey, ack);

            // ✅ 실시간 이벤트(pbp.append)
            if (result.getEvents() != null && !result.getEvents().isEmpty()) {
                result.getEvents().forEach(evt ->
                        messaging.convertAndSend("/topic/scrimmages." + gameId + ".public", evt)
                );
            }

            // ✅ 전체 상태(state.sync)
            if (result.getStateSync() != null) {
                messaging.convertAndSend("/topic/scrimmages." + gameId + ".public", result.getStateSync());
            }

        } catch (Exception e) {
            log.error("❌ Scrimmage command error: {}", e.getMessage(), e);

            // ✅ 에러 응답 (Map.of → 안전한 HashMap 사용)
            Map<String, Object> err = new HashMap<>();
            err.put("type", "error");
            err.put("action", action);
            err.put("meta", meta);

            Map<String, Object> data = new HashMap<>();
            data.put("code", 400);
            data.put("message", e.getMessage());
            err.put("data", data);

            messaging.convertAndSendToUser(
                    username,
                    "/queue/scrimmages." + gameId + ".ack",
                    err
            );
        }
    }
}
