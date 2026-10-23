package com.studyia.backend.web;

import com.studyia.backend.model.Roadmap;
import java.time.Instant;
import java.util.UUID;

public record RoadmapResponse(
        UUID id,
        UUID studentId,
        String title,
        String level,
        String summary,
        String structuredContent,
        Instant generatedAt
) {
    public static RoadmapResponse from(Roadmap roadmap) {
        return new RoadmapResponse(roadmap.getId(), roadmap.getStudentId(), roadmap.getTitle(), roadmap.getLevel(),
            roadmap.getSummary(), roadmap.getStructuredContent(), roadmap.getGeneratedAt());
    }
}
