package com.studyia.backend.repository;

import com.studyia.backend.model.Assessment;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssessmentRepository extends JpaRepository<Assessment, UUID> {
}
