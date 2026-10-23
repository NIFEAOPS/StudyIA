package com.studyia.backend.service;

import com.studyia.backend.model.EmailVerification;
import com.studyia.backend.model.User;
import com.studyia.backend.repository.EmailVerificationRepository;
import com.studyia.backend.repository.UserRepository;
import com.studyia.backend.security.InvalidTokenException;
import java.security.SecureRandom;
import java.time.Instant;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmailVerificationService {

    private static final int CODE_MAX_ATTEMPTS = 5;
    private static final long CODE_LIFETIME_SECONDS = 600;

    private final EmailVerificationRepository verificationRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final SecureRandom random = new SecureRandom();

    public EmailVerificationService(EmailVerificationRepository verificationRepository, UserRepository userRepository,
                                    PasswordEncoder passwordEncoder, EmailService emailService) {
        this.verificationRepository = verificationRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Transactional
    public void createAndSend(User user) {
        verificationRepository.deleteByUserId(user.getId());
        String code = String.format("%06d", random.nextInt(1_000_000));
        verificationRepository.save(new EmailVerification(user, passwordEncoder.encode(code),
                Instant.now().plusSeconds(CODE_LIFETIME_SECONDS)));
        emailService.sendVerificationCode(user.getEmail(), code);
    }

    @Transactional
    public User verify(String email, String code) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Código inválido o expirado"));
        if (user.isEmailVerified()) {
            return user;
        }
        EmailVerification verification = verificationRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Código inválido o expirado"));
        if (verification.getAttempts() >= CODE_MAX_ATTEMPTS || verification.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Código inválido o expirado");
        }
        verification.incrementAttempts();
        if (!passwordEncoder.matches(code, verification.getCodeHash())) {
            throw new IllegalArgumentException("Código inválido o expirado");
        }
        user.verifyEmail();
        verificationRepository.delete(verification);
        return user;
    }

    @Transactional
    public void resend(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("No se pudo reenviar el código"));
        if (user.isEmailVerified()) {
            throw new IllegalArgumentException("El correo ya está verificado");
        }
        createAndSend(user);
    }
}