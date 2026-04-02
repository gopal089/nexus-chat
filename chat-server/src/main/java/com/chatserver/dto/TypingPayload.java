package com.chatserver.dto;

import lombok.*;

import java.util.UUID;

/**
 * Lightweight payload sent when a user starts or stops typing.
 * Never persisted – broadcast only.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TypingPayload {

    private UUID    roomId;
    private String  username;

    /** {@code true} = user started typing; {@code false} = stopped. */
    private boolean typing;
}
