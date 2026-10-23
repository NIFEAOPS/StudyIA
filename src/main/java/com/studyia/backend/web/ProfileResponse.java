package com.studyia.backend.web;

import com.studyia.backend.model.User;
import java.util.UUID;

public record ProfileResponse(UUID userId, String email, String name, String university, String phone, String role, boolean emailVerified,
                              long roadmapCount, int roadmapLimit) {
    public static ProfileResponse from(User user, long roadmapCount) {
        return new ProfileResponse(user.getId(), user.getEmail(), user.getName(), user.getUniversity(), user.getPhone(), user.getRole().name(),
                user.isEmailVerified(), roadmapCount, 3);
    }
}
