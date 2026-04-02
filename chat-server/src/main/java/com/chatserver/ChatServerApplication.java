package com.chatserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Entry point for the Real-Time Group Chat Server.
 *
 * <p>Features:
 * <ul>
 *   <li>WebSocket / STOMP messaging with SimpleMessageBroker</li>
 *   <li>JWT-secured REST + WebSocket endpoints</li>
 *   <li>PostgreSQL persistence via Spring Data JPA + Flyway migrations</li>
 *   <li>Redis caching for active-users and recent room history</li>
 *   <li>Local filesystem file storage (StorageService interface)</li>
 * </ul>
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
public class ChatServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatServerApplication.class, args);
    }
}
