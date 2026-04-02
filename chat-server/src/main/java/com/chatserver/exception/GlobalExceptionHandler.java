package com.chatserver.exception;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.Instant;
import java.util.stream.Collectors;

/**
 * Centralised exception handler – converts exceptions into structured
 * {@link ApiError} JSON responses.
 *
 * <table border="1">
 * <tr>
 * <th>Exception</th>
 * <th>HTTP Status</th>
 * </tr>
 * <tr>
 * <td>MaxUploadSizeExceededException</td>
 * <td>413 Payload Too Large</td>
 * </tr>
 * <tr>
 * <td>AccessDeniedException</td>
 * <td>403 Forbidden</td>
 * </tr>
 * <tr>
 * <td>AuthenticationException</td>
 * <td>401 Unauthorized</td>
 * </tr>
 * <tr>
 * <td>EntityNotFoundException</td>
 * <td>404 Not Found</td>
 * </tr>
 * <tr>
 * <td>IllegalArgumentException</td>
 * <td>400 Bad Request</td>
 * </tr>
 * <tr>
 * <td>MethodArgumentNotValidException</td>
 * <td>400 Bad Request (field errors)</td>
 * </tr>
 * <tr>
 * <td>RuntimeException (fallback)</td>
 * <td>500 Internal Server Error</td>
 * </tr>
 * </table>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ──────────────────────────────────────────────────────────────────
    // File Upload
    // ──────────────────────────────────────────────────────────────────

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiError> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        log.warn("File upload rejected – size limit exceeded: {}", ex.getMessage());
        return error(HttpStatus.PAYLOAD_TOO_LARGE,
                "File size exceeds the maximum allowed limit (50 MB)");
    }

    // ──────────────────────────────────────────────────────────────────
    // Security
    // ──────────────────────────────────────────────────────────────────

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return error(HttpStatus.FORBIDDEN, "You do not have permission to perform this action");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthentication(AuthenticationException ex) {
        log.warn("Authentication failed: {}", ex.getMessage());
        return error(HttpStatus.UNAUTHORIZED, "Authentication failed: " + ex.getMessage());
    }

    // ──────────────────────────────────────────────────────────────────
    // Data / Validation
    // ──────────────────────────────────────────────────────────────────

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> handleEntityNotFound(EntityNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));

        return error(HttpStatus.BAD_REQUEST, message);
    }

    // ──────────────────────────────────────────────────────────────────
    // Catch-all
    // ──────────────────────────────────────────────────────────────────

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiError> handleRuntime(RuntimeException ex, WebRequest request) {

        ex.printStackTrace(); // 🔥 SHOWS FULL ERROR CLEARLY

        return error(HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getMessage()); // 🔥 return real message temporarily
    }

    // ──────────────────────────────────────────────────────────────────
    // Private helper
    // ──────────────────────────────────────────────────────────────────

    private ResponseEntity<ApiError> error(HttpStatus status, String message) {
        ApiError body = ApiError.builder()
                .status(status.value())
                .message(message)
                .timestamp(Instant.now())
                .build();
        return ResponseEntity.status(status).body(body);
    }
}
