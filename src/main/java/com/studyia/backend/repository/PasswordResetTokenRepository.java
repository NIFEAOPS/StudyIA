package com.studyia.backend.repository;

import com.studyia.backend.model.PasswordResetToken;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, UUID> {
    void deleteByUserId(UUID userId);
    Optional<PasswordResetToken> findByTokenHashAndUsedAtIsNull(String tokenHash);
}
