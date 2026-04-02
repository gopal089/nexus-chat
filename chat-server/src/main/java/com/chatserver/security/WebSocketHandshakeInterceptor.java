package com.chatserver.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * Authenticates WebSocket upgrade requests by reading the JWT
 * from the {@code ?token=} query parameter and storing the resolved
 * username in the WebSocket session attributes.
 *
 * <p>The STOMP {@link org.springframework.messaging.simp.stomp.StompHeaderAccessor}
 * can later retrieve the username from {@code sessionAttributes}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    private static final String TOKEN_PARAM   = "token";
    private static final String USERNAME_ATTR = "username";

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Validates the {@code ?token=} query parameter before the WebSocket upgrade.
     * Sets {@code username} in session attributes so controllers can identify the sender.
     *
     * @return {@code true} to allow upgrade; {@code false} to reject (401)
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest          request,
                                   ServerHttpResponse         response,
                                   WebSocketHandler           wsHandler,
                                   Map<String, Object>        attributes) {

        String query = request.getURI().getQuery();
        if (query == null) {
            log.warn("WebSocket handshake rejected – no query string");
            return false;
        }

        String token = extractParam(query, TOKEN_PARAM);
        if (token == null || !jwtTokenProvider.validateToken(token)) {
            log.warn("WebSocket handshake rejected – invalid or missing JWT");
            return false;
        }

        String username = jwtTokenProvider.getUsername(token);
        attributes.put(USERNAME_ATTR, username);
        log.debug("WebSocket handshake granted for user '{}'", username);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest  request,
                               ServerHttpResponse response,
                               WebSocketHandler   wsHandler,
                               Exception          exception) {
        // nothing to do post-handshake
    }

    // ──────────────────────────────────────────────────────────────────
    // Private helpers
    // ──────────────────────────────────────────────────────────────────

    private String extractParam(String query, String param) {
        for (String part : query.split("&")) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2 && param.equals(kv[0])) {
                return kv[1];
            }
        }
        return null;
    }
}
