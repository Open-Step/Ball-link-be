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
import java.util.*;

@Controller
@RequiredArgsConstructor
public class ScoreStompController {

    private final SimpMessagingTemplate messaging;
    private final GameCommandService commandService;
    private final IdempotencyCache idempotencyCache;

    /** ✅ 클라이언트 → /app/games.{gameId}.cmd */
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
        String userName = principal != null ? principal.getName() : "anonymous";

        try {
            // 멱등성 검사
            if (idempotencyKey != null && idempotencyCache.seen(idemKey)) {
                idempotencyCache.get(idemKey).ifPresent(cached ->
                        messaging.convertAndSendToUser(
                                userName,
                                "/queue/games." + gameId + ".ack",
                                cached
                        )
                );
                return;
            }

            // 실제 명령 처리
            GameResult result = commandService.handleCommand(gameId, message);

            // ACK 전송
            Map<String, Object> ack = new HashMap<>();
            ack.put("type", "ack");
            ack.put("action", action);
            ack.put("meta", meta);
            ack.put("data", Map.of("ok", true));

            messaging.convertAndSendToUser(
                    userName,
                    "/queue/games." + gameId + ".ack",
                    ack
            );

            if (idempotencyKey != null)
                idempotencyCache.put(idemKey, ack);

            // 이벤트 브로드캐스트
            if (result.getEvents() != null && !result.getEvents().isEmpty()) {
                result.getEvents().forEach(evt ->
                        messaging.convertAndSend("/topic/games." + gameId + ".public", evt)
                );
            }

            // 전체 상태(state.sync)
            if (result.getStateSync() != null) {
                messaging.convertAndSend("/topic/games." + gameId + ".public", result.getStateSync());
            }

        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("type", "error");
            err.put("action", action);
            err.put("data", Map.of("code", 400, "message", e.getMessage()));
            err.put("meta", meta);

            messaging.convertAndSendToUser(
                    userName,
                    "/queue/games." + gameId + ".ack",
                    err
            );
        }
    }
}
