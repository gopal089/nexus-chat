package com.chatserver.controller;

import com.chatserver.dto.ChatMessagePayload;
import com.chatserver.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for message history.
 *
 * <pre>
 * GET /api/messages/{roomId}?page=0&size=20
 *   → Page&lt;ChatMessagePayload&gt; (newest first)
 * </pre>
 *
 * Supports pagination via standard Spring Data {@code page} / {@code size} params.
 */
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final ChatMessageService messageService;

    /**
     * Returns paginated message history for a given room.
     *
     * @param roomId target room UUID
     * @param page   zero-based page index (default 0)
     * @param size   page size, max 100 (default 20)
     */
    @GetMapping("/{roomId}")
    public ResponseEntity<Page<ChatMessagePayload>> getHistory(
            @PathVariable UUID roomId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<ChatMessagePayload> history = messageService.getHistory(roomId, page, size);
        return ResponseEntity.ok(history);
    }
}
