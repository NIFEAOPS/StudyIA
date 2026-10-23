package com.studyia.backend.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record StudentProfileRequest(
        String learningPath,
        @NotBlank String name,
        @NotBlank @Email String email,
        @Min(2) @Max(8) Integer weeklyAvailabilityHours,
        @Min(3) @Max(12) Integer durationMonths,
        @NotBlank String professionalGoals
) {
    public boolean hasSupportedDuration() {
        return java.util.Set.of(3, 6, 9, 12).contains(durationMonths);
    }
}
