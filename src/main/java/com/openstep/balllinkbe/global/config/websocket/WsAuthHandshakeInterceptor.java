package com.openstep.balllinkbe.global.config.websocket;

import com.openstep.balllinkbe.features.score.repository.ScoreSessionRepository;
import com.openstep.balllinkbe.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class WsAuthHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final ScoreSessionRepository scoreSessionRepository;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {

        String path = request.getURI().getPath();

        // 1. SockJS handshake 사전 요청(/info)은 인증 없이 통과시킴
        if (path.endsWith("/info")) {
            return true;
        }

        // 2. 실제 WebSocket handshake일 때만 인증 검증
        var uri = request.getURI();
        var qp = UriComponentsBuilder.fromUri(uri).build().getQueryParams();

        Long gameId = qp.getFirst("gameId") != null ? Long.valueOf(qp.getFirst("gameId")) : null;
        String sessionToken = qp.getFirst("session");
        String jwt = qp.getFirst("access_token");

        if (gameId == null || sessionToken == null || jwt == null) {
            return false;
        }

        // JWT 토큰 검증
        if (!jwtTokenProvider.validateToken(jwt)) {
            return false;
        }
        Long userId = jwtTokenProvider.getUserId(jwt);

        // 세션 토큰 검증 (score_sessions)
        var scoreSession = scoreSessionRepository
                .findByGameIdAndSessionTokenAndStatus(gameId, sessionToken, "ACTIVE")
                .orElse(null);
        if (scoreSession == null) {
            return false;
        }

        // 권한 설정
        String role = scoreSession.getCreatedBy().getId().equals(userId)
                ? "CONTROLLER"
                : "VIEWER";

        attributes.put("gameId", gameId);
        attributes.put("userId", userId);
        attributes.put("role", role);

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
    }
}
