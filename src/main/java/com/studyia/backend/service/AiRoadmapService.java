package com.studyia.backend.service;

import com.studyia.backend.model.Student;
import java.util.List;
import java.util.Map;

public interface AiRoadmapService {
    AiRoadmapResult generateRoadmap(Student student);

    AiRoadmapResult generateRoadmap(Student student, String assessmentSummary);

    AiAssessmentResult generateAssessment(Student student);

    record AiRoadmapResult(String title, String summary, String structuredContent) {
    }

    record AiAssessmentResult(List<AssessmentQuestion> questions, Map<Integer, String> correctAnswers) {
    }

    record AssessmentQuestion(Integer id, String text, List<AssessmentOption> options) {
    }

    record AssessmentOption(String id, String text) {
    }
}
