package com.openstep.balllinkbe.global.config.websocket;

import com.openstep.balllinkbe.features.score.repository.ScoreSessionRepository;
import com.openstep.balllinkbe.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;
import java.security.Principal;

import java.util.Map;

@Slf4j
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
        if (path.contains("/info")) {
            System.out.println("[WS] /info handshake bypassed");
            return true;
        }

        var uri = request.getURI();
        var qp = UriComponentsBuilder.fromUri(uri).build().getQueryParams();

        Long gameId = qp.getFirst("gameId") != null ? Long.valueOf(qp.getFirst("gameId")) : null;
        String sessionToken = qp.getFirst("session");
        String jwt = qp.getFirst("access_token");

        System.out.println("[WS] gameId=" + gameId + ", session=" + sessionToken + ", jwt=" + (jwt != null));

        if (gameId == null || sessionToken == null || jwt == null) {
            System.out.println("[WS] missing params");
            return false;
        }

        if (!jwtTokenProvider.validateToken(jwt)) {
            System.out.println("[WS] invalid jwt");
            return false;
        }

        Long userId = jwtTokenProvider.getUserId(jwt);
        System.out.println("[WS] jwt valid, userId=" + userId);

        var scoreSession = scoreSessionRepository
                .findByGameIdAndSessionTokenAndStatus(gameId, sessionToken, "ACTIVE")
                .orElse(null);

        if (scoreSession == null) {
            System.out.println("[WS] score session not found");
            return false;
        }

        System.out.println("[WS] score session found, created_by=" + scoreSession.getCreatedBy());

        String role = (scoreSession.getCreatedBy() != null &&
                scoreSession.getCreatedBy().getId().equals(userId))
                ? "CONTROLLER"
                : "VIEWER";

        log.info("[WS-CONNECT] userId={}, gameId={}, sessionToken={}, role={}",
                userId,
                gameId,
                sessionToken,
                role);

        attributes.put("gameId", gameId);
        attributes.put("userId", userId);
        attributes.put("role", role);
        attributes.put("principal", (Principal) () -> String.valueOf(userId)); // 추가

        System.out.println("[WS] handshake success, role=" + role);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
    }
}
