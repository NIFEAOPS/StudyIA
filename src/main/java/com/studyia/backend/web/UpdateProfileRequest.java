package com.studyia.backend.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank @Size(max = 160) String university,
        @Size(max = 30) String phone
) {
}
