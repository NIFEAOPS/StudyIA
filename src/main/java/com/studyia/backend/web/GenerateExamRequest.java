package com.studyia.backend.web;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GenerateExamRequest(
        @NotBlank String learningPath,
        @NotNull @Min(2) @Max(8) Integer weeklyAvailabilityHours,
        @NotNull @Min(3) @Max(12) Integer durationMonths,
        @NotBlank String professionalGoals
) {
    public boolean hasSupportedDuration() {
        return java.util.Set.of(3, 6, 9, 12).contains(durationMonths);
    }
}
