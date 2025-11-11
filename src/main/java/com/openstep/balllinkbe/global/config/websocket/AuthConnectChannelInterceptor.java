package com.openstep.balllinkbe.global.config.websocket;

import com.openstep.balllinkbe.features.score.repository.ScoreSessionRepository;
import com.openstep.balllinkbe.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.*;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AuthConnectChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final ScoreSessionRepository scoreSessionRepository;

    private static String stripBearer(String token) {
        return token != null && token.startsWith("Bearer ") ? token.substring(7) : token;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        var accessor = StompHeaderAccessor.wrap(message);

        // ✅ CONNECT 프레임 시 인증/권한 검증
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = Optional.ofNullable(accessor.getFirstNativeHeader("Authorization"))
                    .orElse(accessor.getFirstNativeHeader("access_token"));
            String session = accessor.getFirstNativeHeader("session");
            String gameIdStr = accessor.getFirstNativeHeader("gameId");

            if (token == null || session == null || gameIdStr == null) {
                throw new MessagingException("Missing authentication parameters");
            }

            token = stripBearer(token);
            if (!jwtTokenProvider.validateToken(token)) {
                throw new MessagingException("Invalid JWT token");
            }

            Long userId = jwtTokenProvider.getUserId(token);
            Long gameId = Long.valueOf(gameIdStr);

            var scoreSession = scoreSessionRepository
                    .findByGameIdAndSessionTokenAndStatus(gameId, session, "ACTIVE")
                    .orElseThrow(() -> new MessagingException("Invalid or expired score session"));

            String role = (scoreSession.getCreatedBy() != null &&
                    scoreSession.getCreatedBy().getId().equals(userId))
                    ? "CONTROLLER" : "VIEWER";

            accessor.setUser(() -> String.valueOf(userId)); // ✅ Principal 설정
            accessor.getSessionAttributes().put("role", role);
            accessor.getSessionAttributes().put("gameId", gameId);

            System.out.printf("[WS-CONNECT] ✅ userId=%s, gameId=%s, role=%s%n", userId, gameId, role);
        }

        return message;
    }
}
