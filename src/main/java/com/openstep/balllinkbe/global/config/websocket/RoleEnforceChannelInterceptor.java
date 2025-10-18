package com.openstep.balllinkbe.global.config.websocket;

import org.springframework.messaging.*;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class RoleEnforceChannelInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor acc = StompHeaderAccessor.wrap(message);
        if (StompCommand.SEND.equals(acc.getCommand())) {
            Map<String, Object> attrs = acc.getSessionAttributes();
            if (attrs == null) return message;

            String role = (String) attrs.get("role");
            String dest = acc.getDestination();

            if (dest != null && dest.startsWith("/app/games.") && dest.endsWith(".cmd")) {
                if (!"CONTROLLER".equals(role)) {
                    throw new MessagingException("Forbidden: VIEWER cannot send commands");
                }
            }
        }
        return message;
    }
}
