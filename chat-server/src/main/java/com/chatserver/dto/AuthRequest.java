package com.chatserver.dto;

import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Request body for {@code POST /api/auth/register} and {@code POST /api/auth/login}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    /** Only required for registration. */
    @Email(message = "Must be a valid email address")
    private String email;
}
