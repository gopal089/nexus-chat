package com.chatserver.controller;

import com.chatserver.dto.AuthRequest;
import com.chatserver.dto.AuthResponse;
import com.chatserver.entity.User;
import com.chatserver.security.JwtTokenProvider;
import com.chatserver.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller exposing authentication endpoints (no JWT required).
 *
 * <pre>
 * POST /api/auth/register  – Register a new user → returns JWT
 * POST /api/auth/login     – Authenticate existing user → returns JWT
 * </pre>
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService           userService;
    private final JwtTokenProvider      jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    /**
     * Registers a new user and immediately issues a JWT.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody AuthRequest req) {
        User user = userService.register(req.getUsername(), req.getEmail(), req.getPassword());

        String token = jwtTokenProvider.generateToken(user);
        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .build());
    }

    /**
     * Authenticates a user with username + password and returns a JWT.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest req) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
        );

        User user = (User) auth.getPrincipal();
        String token = jwtTokenProvider.generateToken(user);

        return ResponseEntity.ok(AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .build());
    }
}
