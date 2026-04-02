package com.chatserver.controller;

import com.chatserver.dto.ChatMessagePayload;
import com.chatserver.dto.TypingPayload;
import com.chatserver.entity.ChatMessage;
import com.chatserver.entity.User;
import com.chatserver.service.CacheService;
import com.chatserver.service.ChatMessageService;
import com.chatserver.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.util.Map;
import java.util.UUID;

/**
 * STOMP WebSocket controller.
 *
 * <table border="1">
 * <tr>
 * <th>Client sends to</th>
 * <th>Broadcast to</th>
 * <th>Action</th>
 * </tr>
 * <tr>
 * <td>/app/chat.send</td>
 * <td>/topic/room.{roomId}</td>
 * <td>Persist + broadcast message</td>
 * </tr>
 * <tr>
 * <td>/app/chat.typing</td>
 * <td>/topic/room.{roomId}.typing</td>
 * <td>Broadcast typing event (no persist)</td>
 * </tr>
 * <tr>
 * <td>/app/chat.join</td>
 * <td>/topic/room.{roomId}</td>
 * <td>Broadcast join notification</td>
 * </tr>
 * </table>
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessageService messageService;
    private final UserService userService;
    private final CacheService cacheService;
    private final SimpMessagingTemplate messagingTemplate;

    // ──────────────────────────────────────────────────────────────────
    // Send Message
    // ──────────────────────────────────────────────────────────────────

    /**
     * Receives a TEXT message from a client, persists it, and broadcasts it
     * to all subscribers of {@code /topic/room.{roomId}}.
     *
     * @param payload        the incoming message
     * @param headerAccessor provides access to session attributes (username)
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessagePayload payload,
            SimpMessageHeaderAccessor headerAccessor) {

        String username = getUsername(headerAccessor);
        log.debug("Received message request from '{}' for room {}: {}", username, payload.getRoomId(),
                payload.getContent());

        User sender = (User) userService.loadUserByUsername(username);

        ChatMessagePayload saved = messageService.save(
                payload.getRoomId(),
                sender,
                payload.getContent(),
                ChatMessage.MessageType.TEXT,
                null, null);

        String destination = "/topic/room." + payload.getRoomId();
        log.debug("Broadcasting message to {}: {}", destination, saved.getContent());
        messagingTemplate.convertAndSend(destination, saved);
    }

    // ──────────────────────────────────────────────────────────────────
    // Typing Indicator
    // ──────────────────────────────────────────────────────────────────

    /**
     * Broadcasts a typing indicator to a room. <strong>Not persisted.</strong>
     */
    @MessageMapping("/chat.typing")
    public void typingIndicator(@Payload TypingPayload payload,
            SimpMessageHeaderAccessor headerAccessor) {

        payload.setUsername(getUsername(headerAccessor));
        messagingTemplate.convertAndSend(
                "/topic/room." + payload.getRoomId() + ".typing", payload);
    }

    // ──────────────────────────────────────────────────────────────────
    // Join Room
    // ──────────────────────────────────────────────────────────────────

    /**
     * Notifies the room that a user has joined and registers them in the
     * active-users Redis cache.
     */
    @MessageMapping("/chat.join")
    public void joinRoom(@Payload Map<String, String> body,
            SimpMessageHeaderAccessor headerAccessor) {

        String username = getUsername(headerAccessor);
        String roomIdStr = body.get("roomId");
        log.debug("Received join request for roomId: {} from user: {}", roomIdStr, username);

        UUID roomId = UUID.fromString(roomIdStr);

        cacheService.addActiveUser(roomId, username);

        ChatMessagePayload notification = ChatMessagePayload.builder()
                .roomId(roomId)
                .senderUsername("SYSTEM")
                .content(username + " joined the room")
                .messageType(ChatMessage.MessageType.TEXT)
                .build();

        String destination = "/topic/room." + roomId;
        log.debug("Broadcasting join notice to {}: {}", destination, notification.getContent());
        messagingTemplate.convertAndSend(destination, notification);
        log.info("User '{}' joined room {}", username, roomId);
    }

    // ──────────────────────────────────────────────────────────────────
    // Private helpers
    // ──────────────────────────────────────────────────────────────────

    private String getUsername(SimpMessageHeaderAccessor accessor) {
        Map<String, Object> attrs = accessor.getSessionAttributes();
        if (attrs == null || !attrs.containsKey("username")) {
            throw new IllegalStateException("WebSocket session has no authenticated user");
        }
        return (String) attrs.get("username");
    }
}
