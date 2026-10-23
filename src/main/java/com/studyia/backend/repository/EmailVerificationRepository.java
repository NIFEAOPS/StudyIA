package com.studyia.backend.repository;

import com.studyia.backend.model.EmailVerification;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, UUID> {
    Optional<EmailVerification> findByUserId(UUID userId);
    void deleteByUserId(UUID userId);
}