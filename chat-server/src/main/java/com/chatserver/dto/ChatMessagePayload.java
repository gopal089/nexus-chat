package com.chatserver.dto;

import com.chatserver.entity.ChatMessage.MessageType;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Payload transferred over WebSocket STOMP and returned by the REST history API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessagePayload {

    private UUID        roomId;
    private String      senderUsername;
    private String      content;
    private MessageType messageType;

    /** Populated for FILE messages; null for TEXT messages. */
    private String fileUrl;
    private String fileName;

    private Instant sentAt;
}
