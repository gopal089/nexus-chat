package com.chatserver.controller;

import com.chatserver.entity.ChatRoom;
import com.chatserver.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for chat rooms.
 * 
 * <pre>
 * GET  /api/rooms       → List all rooms
 * POST /api/rooms       → Create a new room
 * </pre>
 */
@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final ChatRoomService chatRoomService;

    @GetMapping
    public ResponseEntity<List<ChatRoom>> listRooms() {
        return ResponseEntity.ok(chatRoomService.findAll());
    }

    @PostMapping
    public ResponseEntity<ChatRoom> createRoom(@RequestBody RoomRequest req) {
        return ResponseEntity.ok(chatRoomService.createRoom(req.name(), req.description()));
    }

    public record RoomRequest(String name, String description) {
    }
}
