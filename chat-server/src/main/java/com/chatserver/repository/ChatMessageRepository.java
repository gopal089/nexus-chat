package com.chatserver.repository;

import com.chatserver.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    /**
     * Returns paginated messages for a given room, newest first.
     * Using a JOIN FETCH to avoid N+1 queries on sender.
     */
    @Query("SELECT m FROM ChatMessage m " +
           "JOIN FETCH m.sender " +
           "WHERE m.room.id = :roomId " +
           "ORDER BY m.sentAt DESC")
    Page<ChatMessage> findByRoomId(@Param("roomId") UUID roomId, Pageable pageable);
}
