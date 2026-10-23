package com.studyia.backend.service;

import com.studyia.backend.model.Roadmap;
import com.studyia.backend.model.Student;
import com.studyia.backend.repository.RoadmapRepository;
import com.studyia.backend.repository.StudentRepository;
import com.studyia.backend.model.User;
import com.studyia.backend.web.RoadmapResponse;
import com.studyia.backend.web.StudentProfileRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoadmapGenerationService {

    private final StudentRepository studentRepository;
    private final RoadmapRepository roadmapRepository;
    private final AiRoadmapService aiRoadmapService;
    private final ProgressService progressService;

    public RoadmapGenerationService(StudentRepository studentRepository,
                                    RoadmapRepository roadmapRepository,
                                    AiRoadmapService aiRoadmapService,
                                    ProgressService progressService) {
        this.studentRepository = studentRepository;
        this.roadmapRepository = roadmapRepository;
        this.aiRoadmapService = aiRoadmapService;
        this.progressService = progressService;
    }

    @Transactional
    public RoadmapResponse generate(StudentProfileRequest request) {
        return generate(request, null);
    }

    @Transactional
    public RoadmapResponse generate(StudentProfileRequest request, User user) {
        if (user != null && roadmapRepository.countByStudentUserId(user.getId()) >= 3) {
            throw new IllegalStateException("Has alcanzado el límite de 3 roadmaps por usuario");
        }
        if (request.weeklyAvailabilityHours() == null
                || request.weeklyAvailabilityHours() < 2
                || request.weeklyAvailabilityHours() > 8) {
            throw new IllegalArgumentException("Las horas disponibles deben estar entre 2 y 8 por semana");
        }
        if (!request.hasSupportedDuration()) {
            throw new IllegalArgumentException("La duración debe ser de 3, 6, 9 o 12 meses");
        }
        Student student = new Student(user, user == null ? request.name() : user.getName(),
            user == null ? request.email() : user.getEmail(), request.learningPath(),
            request.weeklyAvailabilityHours(), request.durationMonths(), request.professionalGoals());
        Student savedStudent = studentRepository.save(student);
        AiRoadmapService.AiRoadmapResult generated = aiRoadmapService.generateRoadmap(savedStudent);
        Roadmap roadmap = new Roadmap(savedStudent, generated.title(), generated.summary(),
                generated.structuredContent());
        progressService.createStructure(roadmap);
        return RoadmapResponse.from(roadmapRepository.save(roadmap));
    }
}
