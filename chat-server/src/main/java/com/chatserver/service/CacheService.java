package com.chatserver.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

/**
 * Redis-backed cache service for real-time data that doesn't warrant
 * a full database round-trip on every WebSocket event.
 *
 * <ul>
 *   <li><strong>Active Users</strong>: a Redis Set per room that expires after 5 minutes.
 *       "Login" refreshes the TTL; client heartbeats can call {@code addActiveUser} periodically.</li>
 *   <li><strong>Recent Messages</strong>: a simple string key pointing to a JSON payload.
 *       The REST history endpoint populates this; WebSocket broadcasts clear it.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private static final Duration ACTIVE_USER_TTL   = Duration.ofMinutes(5);
    private static final String   ACTIVE_USERS_KEY  = "active-users:";

    private final RedisTemplate<String, String> redisTemplate;

    // ──────────────────────────────────────────────────────────────
    // Active Users
    // ──────────────────────────────────────────────────────────────

    /**
     * Registers a user as active in a room and refreshes the expiry.
     */
    public void addActiveUser(UUID roomId, String username) {
        String key = ACTIVE_USERS_KEY + roomId;
        redisTemplate.opsForSet().add(key, username);
        redisTemplate.expire(key, ACTIVE_USER_TTL);
        log.debug("Active user '{}' added to room {}", username, roomId);
    }

    /**
     * Removes a user from the active-users set (e.g. on disconnect).
     */
    public void removeActiveUser(UUID roomId, String username) {
        String key = ACTIVE_USERS_KEY + roomId;
        redisTemplate.opsForSet().remove(key, username);
        log.debug("Active user '{}' removed from room {}", username, roomId);
    }

    /**
     * Returns the current set of active usernames for the given room.
     * Returns an empty set if no data is cached (treated as all expired).
     */
    public Set<String> getActiveUsers(UUID roomId) {
        Set<String> members = redisTemplate.opsForSet().members(ACTIVE_USERS_KEY + roomId);
        return members != null ? members : Set.of();
    }
}
