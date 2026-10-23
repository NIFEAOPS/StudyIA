package com.studyia.backend.repository;

import com.studyia.backend.model.Roadmap;
import java.util.UUID;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoadmapRepository extends JpaRepository<Roadmap, UUID> {
    Optional<Roadmap> findFirstByStudentUserIdOrderByGeneratedAtDesc(UUID userId);
    long countByStudentUserId(UUID userId);
}
