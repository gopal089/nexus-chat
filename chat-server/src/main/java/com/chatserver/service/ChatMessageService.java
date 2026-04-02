package com.chatserver.service;

import com.chatserver.dto.ChatMessagePayload;
import com.chatserver.entity.ChatMessage;
import com.chatserver.entity.ChatRoom;
import com.chatserver.entity.User;
import com.chatserver.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Handles persisting and retrieving chat messages.
 */
@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository messageRepository;
    private final ChatRoomService        chatRoomService;

    /**
     * Persists a chat message and returns the full payload for broadcasting.
     */
    @Transactional
    public ChatMessagePayload save(UUID roomId, User sender,
                                   String content, ChatMessage.MessageType type,
                                   String fileUrl, String fileName) {
        ChatRoom room = chatRoomService.findById(roomId);

        ChatMessage msg = ChatMessage.builder()
                .room(room)
                .sender(sender)
                .content(content)
                .messageType(type)
                .fileUrl(fileUrl)
                .fileName(fileName)
                .build();

        ChatMessage saved = messageRepository.save(msg);
        return toPayload(saved);
    }

    /**
     * Returns a page of messages for the room, newest-first (descending sentAt).
     *
     * @param roomId target room
     * @param page   zero-based page index
     * @param size   page size (max 100 enforced here)
     */
    @Transactional(readOnly = true)
    public Page<ChatMessagePayload> getHistory(UUID roomId, int page, int size) {
        int safeSize = Math.min(size, 100);
        Pageable pageable = PageRequest.of(page, safeSize);
        return messageRepository.findByRoomId(roomId, pageable)
                .map(this::toPayload);
    }

    // ──────────────────────────────────────────────────────────────────────
    // Private helpers
    // ──────────────────────────────────────────────────────────────────────

    private ChatMessagePayload toPayload(ChatMessage msg) {
        return ChatMessagePayload.builder()
                .roomId(msg.getRoom().getId())
                .senderUsername(msg.getSender().getUsername())
                .content(msg.getContent())
                .messageType(msg.getMessageType())
                .fileUrl(msg.getFileUrl())
                .fileName(msg.getFileName())
                .sentAt(msg.getSentAt())
                .build();
    }
}
