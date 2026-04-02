package com.chatserver.config;

import com.chatserver.security.WebSocketHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

/**
 * Configures the STOMP WebSocket message broker.
 *
 * <ul>
 *   <li>Endpoint: {@code /ws} (with SockJS fallback)</li>
 *   <li>App destination prefix: {@code /app} – messages routed to {@code @MessageMapping} methods</li>
 *   <li>Broker destinations: {@code /topic} (pub/sub) and {@code /queue} (point-to-point)</li>
 * </ul>
 *
 * <p><strong>Scaling:</strong> Replace {@code enableSimpleBroker} with
 * {@code enableStompBrokerRelay} (+ RabbitMQ/ActiveMQ) when horizontal scaling is needed.
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketHandshakeInterceptor handshakeInterceptor;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .addInterceptors(handshakeInterceptor)
                .setAllowedOriginPatterns("*")   // tighten in production
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Messages sent from clients to server land on /app/...
        registry.setApplicationDestinationPrefixes("/app");

        // Server-to-client broadcasting via simple in-memory broker
        registry.enableSimpleBroker("/topic", "/queue");

        // Prefix for user-specific destinations (e.g. /user/queue/errors)
        registry.setUserDestinationPrefix("/user");
    }
}
