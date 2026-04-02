package com.chatserver.exception;

import lombok.*;

import java.time.Instant;

/**
 * Standard error envelope returned by the {@link GlobalExceptionHandler}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiError {

    private int    status;
    private String message;
    private Instant timestamp;
}
