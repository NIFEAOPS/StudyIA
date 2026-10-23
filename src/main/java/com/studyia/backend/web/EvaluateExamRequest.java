package com.studyia.backend.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record EvaluateExamRequest(
        @NotNull UUID assessmentId,
        @NotEmpty List<@Valid AnswerRequest> answers
) {
    public record AnswerRequest(
            @NotNull Integer questionId,
            @NotBlank String selectedOption
    ) {
    }
}
