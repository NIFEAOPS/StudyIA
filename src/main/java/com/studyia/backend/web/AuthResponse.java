package com.studyia.backend.web;

import com.studyia.backend.model.User;
import java.util.UUID;

public record AuthResponse(
        String accessToken,
        String tokenType,
        long expiresIn,
        UUID userId,
        String email,
        String name,
        String role
) {
    public static AuthResponse from(String accessToken, long expiresIn, User user) {
        return new AuthResponse(accessToken, "Bearer", expiresIn, user.getId(), user.getEmail(),
                user.getName(), user.getRole().name());
    }
}
