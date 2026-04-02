package com.chatserver.config;

import com.chatserver.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Seeds the database with default chat rooms on startup if they don't exist.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final ChatRoomService chatRoomService;

    @Override
    public void run(ApplicationArguments args) {
        seedRoom("global-chat", "The main public chat room");
        seedRoom("random", "Off-topic conversations");
    }

    private void seedRoom(String name, String description) {
        try {
            chatRoomService.createRoom(name, description);
            log.info("Seeded chat room: '{}'", name);
        } catch (IllegalArgumentException e) {
            log.debug("Chat room '{}' already exists, skipping seed.", name);
        }
    }
}
