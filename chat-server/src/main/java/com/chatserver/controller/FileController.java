package com.chatserver.controller;

import com.chatserver.dto.ChatMessagePayload;
import com.chatserver.dto.FileUploadResponse;
import com.chatserver.entity.ChatMessage;
import com.chatserver.entity.User;
import com.chatserver.service.ChatMessageService;
import com.chatserver.service.StorageService;
import com.chatserver.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 * REST endpoint for file uploads.
 *
 * <pre>
 * POST /api/files/upload?roomId={uuid}
 *   Content-Type: multipart/form-data
 *   → FileUploadResponse (url, fileName, fileType, size)
 * </pre>
 *
 * After saving the file the controller both:
 * <ol>
 *   <li>Returns the {@link FileUploadResponse} to the HTTP caller.</li>
 *   <li>Broadcasts a FILE {@link ChatMessagePayload} to the room's WebSocket topic
 *       so all connected clients can render the file link inline.</li>
 * </ol>
 */
@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final StorageService        storageService;
    private final ChatMessageService    messageService;
    private final UserService           userService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Handles multipart file upload, stores the file, persists a FILE message,
     * and broadcasts the file metadata over WebSocket.
     *
     * @param file        the uploaded file (max 50 MB configured in application.yml)
     * @param roomId      the chat room where the file should appear
     * @param userDetails injected from the JWT-authenticated security context
     */
    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponse> upload(
            @RequestParam("file")   MultipartFile file,
            @RequestParam("roomId") UUID          roomId,
            @AuthenticationPrincipal UserDetails  userDetails) {

        // 1 – Store the file
        String downloadUrl = storageService.store(file);
        String contentType = file.getContentType() != null
                ? file.getContentType() : "application/octet-stream";

        FileUploadResponse uploadResponse = FileUploadResponse.builder()
                .url(downloadUrl)
                .fileName(file.getOriginalFilename())
                .fileType(contentType)
                .size(file.getSize())
                .build();

        // 2 – Persist as a FILE ChatMessage
        User sender = (User) userService.loadUserByUsername(userDetails.getUsername());

        ChatMessagePayload wsPayload = messageService.save(
                roomId,
                sender,
                null,                              // no text content for file messages
                ChatMessage.MessageType.FILE,
                downloadUrl,
                file.getOriginalFilename()
        );

        // 3 – Broadcast file metadata to the room topic so chat clients render it inline
        messagingTemplate.convertAndSend("/topic/room." + roomId, wsPayload);

        log.info("File '{}' uploaded by '{}', shared in room {}",
                file.getOriginalFilename(), userDetails.getUsername(), roomId);

        return ResponseEntity.ok(uploadResponse);
    }
}
