package com.studyia.backend.repository;

import com.studyia.backend.model.LearningSession;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LearningSessionRepository extends JpaRepository<LearningSession, UUID> {

    @Query("select s.startedAt from LearningSession s where s.student.id = :studentId and s.startedAt >= :since")
    List<Instant> findStartedAtSince(UUID studentId, Instant since);
}
