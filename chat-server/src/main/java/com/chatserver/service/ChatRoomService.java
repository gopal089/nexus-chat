package com.chatserver.service;

import com.chatserver.entity.ChatRoom;
import com.chatserver.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Business logic for chat rooms.
 * Frequently accessed rooms are cached in Redis via Spring Cache.
 */
@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    @Cacheable(value = "rooms", key = "#id")
    @Transactional(readOnly = true)
    public ChatRoom findById(UUID id) {
        return chatRoomRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException(
                        "Chat room not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<ChatRoom> findAll() {
        return chatRoomRepository.findAll();
    }

    @CacheEvict(value = "rooms", allEntries = true)
    @Transactional
    public ChatRoom createRoom(String name, String description) {
        if (chatRoomRepository.existsByName(name)) {
            throw new IllegalArgumentException("Room '" + name + "' already exists");
        }
        return chatRoomRepository.save(
                ChatRoom.builder().name(name).description(description).build());
    }
}
