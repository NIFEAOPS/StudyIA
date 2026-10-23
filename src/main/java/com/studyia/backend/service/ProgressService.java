package com.studyia.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyia.backend.model.LearningSession;
import com.studyia.backend.model.Roadmap;
import com.studyia.backend.model.RoadmapActivity;
import com.studyia.backend.model.RoadmapStage;
import com.studyia.backend.model.RoadmapResource;
import com.studyia.backend.model.Student;
import com.studyia.backend.model.User;
import com.studyia.backend.repository.LearningSessionRepository;
import com.studyia.backend.repository.RoadmapRepository;
import com.studyia.backend.web.DashboardResponse;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProgressService {

    private final RoadmapRepository roadmapRepository;
    private final LearningSessionRepository learningSessionRepository;
    private final ObjectMapper objectMapper;

    public ProgressService(RoadmapRepository roadmapRepository,
                           LearningSessionRepository learningSessionRepository,
                           ObjectMapper objectMapper) {
        this.roadmapRepository = roadmapRepository;
        this.learningSessionRepository = learningSessionRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public DashboardResponse dashboard(User user) {
        Roadmap roadmap = roadmapRepository.findFirstByStudentUserIdOrderByGeneratedAtDesc(studentId(user))
                .orElseThrow(() -> new EntityNotFoundException("Aún no tienes un roadmap generado"));
        if (roadmap.getStages().isEmpty()) {
            createStructure(roadmap);
            roadmapRepository.save(roadmap);
        }
        validateCalendar(roadmap);
        return DashboardResponse.from(roadmap, currentStreak(roadmap.getStudent()));
    }

    @Transactional
    public DashboardResponse completeActivity(User user, UUID activityId) {
        Roadmap roadmap = roadmapRepository.findFirstByStudentUserIdOrderByGeneratedAtDesc(studentId(user))
                .orElseThrow(() -> new EntityNotFoundException("Aún no tienes un roadmap generado"));
        RoadmapActivity activity = roadmap.getStages().stream()
                .flatMap(stage -> stage.getActivities().stream())
                .filter(candidate -> candidate.getId().equals(activityId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Actividad no encontrada"));
        activity.complete();
        RoadmapStage stage = roadmap.getStages().stream()
                .filter(candidate -> candidate.getActivities().contains(activity))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Etapa no encontrada"));
        if (!stage.getActivities().isEmpty()
                && stage.getActivities().stream().allMatch(candidate -> "COMPLETED".equals(candidate.getStatus()))) {
            stage.setStatus("COMPLETED");
            roadmap.getStages().stream()
                    .filter(candidate -> candidate.getStageNumber() == stage.getStageNumber() + 1
                            && "LOCKED".equals(candidate.getStatus()))
                    .findFirst()
                    .ifPresent(next -> next.setStatus("AVAILABLE"));
        } else {
            stage.setStatus("IN_PROGRESS");
        }
        learningSessionRepository.save(new LearningSession(roadmap.getStudent(), roadmap, activity));
        return DashboardResponse.from(roadmap, currentStreak(roadmap.getStudent()));
    }

    public void createStructure(Roadmap roadmap) {
        try {
            JsonNode root = objectMapper.readTree(roadmap.getStructuredContent());
            JsonNode months = root.path("months");
            if (!months.isArray()) {
                throw new IllegalStateException("El roadmap generado no contiene una lista de meses válida");
            }
            if (months.isEmpty()) {
                throw new IllegalStateException("El roadmap generado no contiene meses");
            }
            int expectedMonths = roadmap.getStudent().getDurationMonths();
            if (months.size() != expectedMonths) {
                throw new IllegalStateException("El roadmap debe contener exactamente " + expectedMonths
                        + " meses para la duración seleccionada");
            }
            int stageNumber = 1;
            Set<Integer> calendarMonths = new HashSet<>();
            for (JsonNode month : months) {
                if (!month.isObject()) {
                    throw new IllegalStateException("Un mes del roadmap no tiene un formato válido");
                }
                int monthNumber = month.path("month").asInt(stageNumber);
                if (monthNumber < 1 || monthNumber > expectedMonths || !calendarMonths.add(monthNumber)) {
                    throw new IllegalStateException("El roadmap contiene una asignación de mes inválida");
                }
                RoadmapStage stage = new RoadmapStage(roadmap, stageNumber, monthNumber, null,
                        text(month, "title", "Mes " + stageNumber),
                        text(month, "objective", "Completar objetivos del mes"));
                int position = 1;
                for (JsonNode topic : month.path("topics")) {
                            String title = topic.isObject()
                                    ? text(topic, "title", "Actividad del mes")
                                    : topic.asText();
                            String description = topic.isObject()
                                    ? text(topic, "instruction", text(topic, "description", title))
                                    : topic.asText();
                            Integer estimatedMinutes = topic.isObject()
                                    ? topic.path("estimatedMinutes").asInt(60)
                                    : 60;
                            String resourceUrl = topic.isObject()
                                    ? text(topic, "resourceUrl", null)
                                    : null;
                            stage.addActivity(new RoadmapActivity(stage, title, description, resourceUrl,
                                    position++, estimatedMinutes));
                        }
                for (JsonNode resource : month.path("resources")) {
                    String resourceUrl = text(resource, "url", text(resource, "link", null));
                    if (resourceUrl != null && !resourceUrl.isBlank()) {
                        stage.addResource(new RoadmapResource(stage, text(resource, "title", "Recurso"),
                                text(resource, "description", "Recurso recomendado"), resourceUrl,
                                text(resource, "type", "RESOURCE"), position++));
                    }
                }
                roadmap.addStage(stage);
                stageNumber++;
            }
        } catch (Exception exception) {
            throw new IllegalStateException("El roadmap generado no tiene un formato válido", exception);
        }
    }

    private String text(JsonNode node, String field, String fallback) {
        String value = node.path(field).asText(null);
        return value == null || value.isBlank() ? fallback : value;
    }

    private void validateCalendar(Roadmap roadmap) {
        int expectedMonths = roadmap.getStudent().getDurationMonths();
        boolean valid = roadmap.getStages().size() == expectedMonths
                && roadmap.getStages().stream().allMatch(stage ->
            stage.getMonthNumber() != null && stage.getWeekOfMonth() == null);
        if (!valid) {
            throw new IllegalStateException("Este roadmap fue creado con un formato anterior. "
                    + "Genera una nueva ruta para obtener " + expectedMonths + " meses organizados.");
        }
    }

    private long currentStreak(Student student) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        var activeDates = learningSessionRepository.findStartedAtSince(student.getId(),
                today.minusDays(365).atStartOfDay().toInstant(ZoneOffset.UTC)).stream()
                .map(instant -> instant.atZone(ZoneOffset.UTC).toLocalDate())
                .collect(java.util.stream.Collectors.toSet());
        long streak = 0;
        for (int offset = 0; offset < 365; offset++) {
            if (activeDates.contains(today.minusDays(offset))) {
                streak++;
            } else if (offset > 0) {
                break;
            }
        }
        return streak;
    }

    private UUID studentId(User user) {
        if (user.getId() == null) {
            throw new AccessDeniedException("Usuario sin estudiante asociado");
        }
        return user.getId();
    }
}
