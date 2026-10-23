package com.studyia.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyia.backend.model.Assessment;
import com.studyia.backend.model.Roadmap;
import com.studyia.backend.model.Student;
import com.studyia.backend.model.User;
import com.studyia.backend.repository.AssessmentRepository;
import com.studyia.backend.repository.RoadmapRepository;
import com.studyia.backend.repository.StudentRepository;
import com.studyia.backend.repository.UserRepository;
import com.studyia.backend.web.EvaluateExamRequest;
import com.studyia.backend.web.ExamResponse;
import com.studyia.backend.web.GenerateExamRequest;
import com.studyia.backend.web.RoadmapResponse;
import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AssessmentService {

    private final AssessmentRepository assessmentRepository;
    private final StudentRepository studentRepository;
    private final RoadmapRepository roadmapRepository;
    private final UserRepository userRepository;
    private final AiRoadmapService aiRoadmapService;
    private final ObjectMapper objectMapper;
    private final ProgressService progressService;

    public AssessmentService(AssessmentRepository assessmentRepository,
                            StudentRepository studentRepository,
                            RoadmapRepository roadmapRepository,
                            UserRepository userRepository,
                            AiRoadmapService aiRoadmapService,
                            ObjectMapper objectMapper,
                            ProgressService progressService) {
        this.assessmentRepository = assessmentRepository;
        this.studentRepository = studentRepository;
        this.roadmapRepository = roadmapRepository;
        this.userRepository = userRepository;
        this.aiRoadmapService = aiRoadmapService;
        this.objectMapper = objectMapper;
        this.progressService = progressService;
    }

    @Transactional
    public ExamResponse generateAssessment(User user, GenerateExamRequest request) {
        if (request.weeklyAvailabilityHours() == null
                || request.weeklyAvailabilityHours() < 2
                || request.weeklyAvailabilityHours() > 8) {
            throw new IllegalArgumentException("Las horas disponibles deben estar entre 2 y 8 por semana");
        }
        if (!request.hasSupportedDuration()) {
            throw new IllegalArgumentException("La duración debe ser de 3, 6, 9 o 12 meses");
        }
        Student student = getOrCreateStudent(user, request);
        AiRoadmapService.AiAssessmentResult generated = aiRoadmapService.generateAssessment(student);

        try {
            String correctAnswersJson = objectMapper.writeValueAsString(generated.correctAnswers());
                String questionsJson = objectMapper.writeValueAsString(generated.questions());
            Assessment assessment = new Assessment(
                    user,
                    student.getName(),
                    student.getEmail(),
                    student.getLearningPath(),
                    student.getProfessionalGoals(),
                    correctAnswersJson,
                    questionsJson
            );
            Assessment savedAssessment = assessmentRepository.save(assessment);
            return ExamResponse.from(savedAssessment.getId(), generated.questions());
        } catch (Exception exception) {
            throw new IllegalStateException("No se pudo guardar el examen generado", exception);
        }
    }

    @Transactional
    public RoadmapResponse evaluateAssessment(User user, EvaluateExamRequest request) {
        enforceRoadmapLimit(user);
        Assessment assessment = assessmentRepository.findById(request.assessmentId())
                .orElseThrow(() -> new EntityNotFoundException("Examen no encontrado"));

        if (user.getId() == null || assessment.getUser().getId() == null
                || !assessment.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("Este examen no pertenece al usuario autenticado");
        }

        Map<Integer, String> correctAnswers = readCorrectAnswers(assessment.getCorrectAnswersJson());
        Map<Integer, String> questionTexts = readQuestionTexts(assessment.getQuestionsJson());
        int score = 0;
        List<String> gaps = new ArrayList<>();

        for (EvaluateExamRequest.AnswerRequest answer : request.answers()) {
            String expected = correctAnswers.get(answer.questionId());
            if (expected != null && expected.equalsIgnoreCase(answer.selectedOption())) {
                score++;
            } else if (expected != null) {
                String questionText = questionTexts.getOrDefault(answer.questionId(), "Enunciado no disponible");
                gaps.add("Pregunta " + answer.questionId() + " (" + questionText + "): esperabas "
                        + expected + ", respondiste " + answer.selectedOption());
            }
        }

        int scorePercentage = percentage(score, correctAnswers.size());
        String summary = "Nivel inferido: " + inferredLevel(scorePercentage) + " | Puntaje: " + scorePercentage + "% | "
                + (gaps.isEmpty() ? "No se detectaron brechas importantes" : "Brechas: " + String.join("; ", gaps));

        Student student = studentRepository.findByUserId(user.getId())
                .orElseGet(() -> new Student(user, assessment.getStudentName(), assessment.getStudentEmail(),
                    assessment.getLearningPath(),
                        8, 3, assessment.getProfessionalGoals()));

        student.setLearningPath(assessment.getLearningPath());
        student.setProfessionalGoals(assessment.getProfessionalGoals());
        studentRepository.save(student);

        AiRoadmapService.AiRoadmapResult generated = aiRoadmapService.generateRoadmap(student, summary);
        Roadmap roadmap = new Roadmap(student, inferredLevel(scorePercentage), generated.title(), generated.summary(), generated.structuredContent());
        progressService.createStructure(roadmap);
        return RoadmapResponse.from(roadmapRepository.save(roadmap));
    }

    private void enforceRoadmapLimit(User user) {
        long roadmapCount = roadmapRepository.countByStudentUserId(user.getId());
        if (roadmapCount >= 3) {
            throw new IllegalStateException("Has alcanzado el límite de 3 roadmaps por usuario");
        }
    }

    private Student getOrCreateStudent(User user, GenerateExamRequest request) {
        Optional<Student> existingStudent = user.getId() == null
                ? Optional.empty()
                : studentRepository.findByUserId(user.getId());
        if (existingStudent != null && existingStudent.isPresent()) {
            Student student = existingStudent.get();
            student.setName(user.getName());
            student.setEmail(user.getEmail());
            student.setLearningPath(request.learningPath());
            student.setWeeklyAvailabilityHours(request.weeklyAvailabilityHours());
            student.setDurationMonths(request.durationMonths());
            student.setProfessionalGoals(request.professionalGoals());
            student.setUser(user);
            return studentRepository.save(student);
        }

        Student newStudent = new Student(user, user.getName(), user.getEmail(), request.learningPath(),
            request.weeklyAvailabilityHours(), request.durationMonths(), request.professionalGoals());
        return studentRepository.save(newStudent);
    }

    private Map<Integer, String> readCorrectAnswers(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<Map<Integer, String>>() {});
        } catch (Exception exception) {
            throw new IllegalStateException("No se pudieron leer las respuestas correctas del examen", exception);
        }
    }

    private int percentage(int correct, int total) {
        if (total == 0) {
            return 0;
        }
        return Math.round((correct * 100f) / total);
    }

    private String inferredLevel(int scorePercentage) {
        if (scorePercentage < 50) {
            return "beginner";
        }
        if (scorePercentage < 80) {
            return "intermediate";
        }
        return "advanced";
    }

    private Map<Integer, String> readQuestionTexts(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            Map<Integer, String> questionTexts = new HashMap<>();
            for (JsonNode question : objectMapper.readTree(json)) {
                questionTexts.put(question.path("id").asInt(), question.path("text").asText());
            }
            return questionTexts;
        } catch (Exception exception) {
            return Map.of();
        }
    }
}
