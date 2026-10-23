package com.studyia.backend.web;

import com.studyia.backend.model.User;
import com.studyia.backend.repository.UserRepository;
import com.studyia.backend.service.AssessmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/assessments")
public class AssessmentController {

    private final AssessmentService assessmentService;
    private final UserRepository userRepository;

    public AssessmentController(AssessmentService assessmentService, UserRepository userRepository) {
        this.assessmentService = assessmentService;
        this.userRepository = userRepository;
    }

    @PostMapping("/generate")
    public ResponseEntity<ExamResponse> generate(
            @Valid @RequestBody GenerateExamRequest request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        User user = userRepository.findByEmailIgnoreCase(principal.getUsername())
                .orElseThrow(() -> new IllegalStateException("Usuario autenticado no encontrado"));
        return ResponseEntity.ok(assessmentService.generateAssessment(user, request));
    }

    @PostMapping("/evaluate")
    public ResponseEntity<RoadmapResponse> evaluate(
            @Valid @RequestBody EvaluateExamRequest request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        User user = userRepository.findByEmailIgnoreCase(principal.getUsername())
                .orElseThrow(() -> new IllegalStateException("Usuario autenticado no encontrado"));
        return ResponseEntity.ok(assessmentService.evaluateAssessment(user, request));
    }
}
