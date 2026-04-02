package com.chatserver.dto;

import lombok.*;

/**
 * Response body containing the JWT token issued after successful auth.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String username;
    private String tokenType = "Bearer";
}
