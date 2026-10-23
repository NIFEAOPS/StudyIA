package com.studyia.backend.web;

import com.studyia.backend.service.AiRoadmapService;
import java.util.List;
import java.util.UUID;

public record ExamResponse(
        UUID assessmentId,
        List<QuestionResponse> questions
) {
    public static ExamResponse from(UUID assessmentId, List<AiRoadmapService.AssessmentQuestion> questions) {
        return new ExamResponse(assessmentId, questions.stream().map(QuestionResponse::from).toList());
    }

    public record QuestionResponse(
            Integer id,
            String text,
            List<OptionResponse> options
    ) {
        public static QuestionResponse from(AiRoadmapService.AssessmentQuestion question) {
            return new QuestionResponse(question.id(), question.text(),
                    question.options().stream().map(OptionResponse::from).toList());
        }
    }

    public record OptionResponse(
            String id,
            String text
    ) {
        public static OptionResponse from(AiRoadmapService.AssessmentOption option) {
            return new OptionResponse(option.id(), option.text());
        }
    }
}
