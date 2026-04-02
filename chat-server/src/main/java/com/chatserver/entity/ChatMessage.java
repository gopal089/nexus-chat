package com.chatserver.entity;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity representing a single chat message.
 * Supports TEXT messages and FILE messages (with metadata).
 */
@Entity
@Table(name = "chat_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    /**
     * Discriminates between plain text and file-share messages.
     */
    public enum MessageType {
        TEXT, FILE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private ChatRoom room;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    /** Body of the message; may be null for FILE-only messages. */
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    /**
     * Uses PostgreSQL-native ENUM via {@code @Enumerated(EnumType.STRING)}.
     * Flyway migration V3 defines the {@code message_type} type.
     */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "message_type", nullable = false)
    private MessageType messageType;

    /** Public URL / path of the uploaded file. Null for TEXT messages. */
    @Column(name = "file_url", length = 500)
    private String fileUrl;

    /** Original file name. Null for TEXT messages. */
    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "sent_at", nullable = false, updatable = false)
    private Instant sentAt;

    @PrePersist
    protected void onCreate() {
        if (sentAt == null) {
            sentAt = Instant.now();
        }
        if (messageType == null) {
            messageType = MessageType.TEXT;
        }
    }
}
