package com.studyia.backend.service;

import com.studyia.backend.model.Role;
import com.studyia.backend.model.User;
import com.studyia.backend.repository.UserRepository;
import com.studyia.backend.security.JwtService;
import com.studyia.backend.security.InvalidTokenException;
import com.studyia.backend.web.AuthResponse;
import com.studyia.backend.web.LoginRequest;
import com.studyia.backend.web.RegisterRequest;
import com.studyia.backend.web.RegistrationResponse;
import com.studyia.backend.web.ChangePasswordRequest;
import com.studyia.backend.web.ResetPasswordRequest;
import com.studyia.backend.model.PasswordResetToken;
import com.studyia.backend.repository.PasswordResetTokenRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import io.jsonwebtoken.JwtException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final EmailVerificationService emailVerificationService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;
    private final String frontendBaseUrl;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, JwtService jwtService,
                       EmailVerificationService emailVerificationService,
                       PasswordResetTokenRepository passwordResetTokenRepository,
                       EmailService emailService,
                       @Value("${studyia.frontend.base-url:http://localhost:5173}") String frontendBaseUrl) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.emailVerificationService = emailVerificationService;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailService = emailService;
        this.frontendBaseUrl = frontendBaseUrl;
    }

    @Transactional
    public RegistrationResponse register(RegisterRequest request) {
        String email = normalize(request.email());
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("El correo ya esta registrado");
        }
        try {
            User user = userRepository.save(new User(email, passwordEncoder.encode(request.password()),
                    request.name().trim(), Role.STUDENT));
            emailVerificationService.createAndSend(user);
            return new RegistrationResponse(user.getEmail(), "Código de verificación enviado");
        } catch (DataIntegrityViolationException exception) {
            throw new IllegalArgumentException("El correo ya esta registrado", exception);
        }
    }

    public AuthTokens login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalize(request.email()), request.password()));
        User user = (User) authentication.getPrincipal();
        if (!user.isEmailVerified()) {
            throw new IllegalArgumentException("Debes verificar tu correo antes de iniciar sesión");
        }
        return tokensFor(user);
    }

    public AuthTokens verifyEmail(String email, String code) {
        return tokensFor(emailVerificationService.verify(normalize(email), code));
    }

    public AuthTokens refresh(String refreshToken) {
        final String username;
        try {
            username = jwtService.extractUsername(refreshToken);
        } catch (JwtException | IllegalArgumentException exception) {
            throw new InvalidTokenException("Refresh token invalido", exception);
        }

        User user = userRepository.findByEmailIgnoreCase(username)
                .orElseThrow(() -> new InvalidTokenException("Refresh token invalido"));
        if (!jwtService.isRefreshTokenValid(refreshToken, user)) {
            throw new InvalidTokenException("Refresh token invalido");
        }
        return new AuthTokens(jwtService.generateAccessToken(user), jwtService.generateRefreshToken(user), user);
    }

    @Transactional
    public void requestPasswordReset(String email) {
        userRepository.findByEmailIgnoreCase(normalize(email)).ifPresent(user -> {
            passwordResetTokenRepository.deleteByUserId(user.getId());
            byte[] rawToken = new byte[32];
            secureRandom.nextBytes(rawToken);
            String token = Base64.getUrlEncoder().withoutPadding().encodeToString(rawToken);
            passwordResetTokenRepository.save(new PasswordResetToken(user, hashToken(token),
                    Instant.now().plusSeconds(600)));
            emailService.sendPasswordResetLink(user.getEmail(),
                    frontendBaseUrl + "/reset-password?token=" + token);
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        PasswordResetToken resetToken = passwordResetTokenRepository
                .findByTokenHashAndUsedAtIsNull(hashToken(request.token()))
                .orElseThrow(() -> new IllegalArgumentException("El enlace no es válido o ya expiró"));
        if (resetToken.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("El enlace no es válido o ya expiró");
        }
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        resetToken.markUsed();
        passwordResetTokenRepository.save(resetToken);
    }

    private String hashToken(String token) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("No se pudo proteger el token de recuperación", exception);
        }
    }

    @Transactional
    public User updateProfile(User user, String university, String phone) {
        user.setUniversity(university.trim());
        user.setPhone(phone == null || phone.isBlank() ? null : phone.trim());
        return userRepository.save(user);
    }

    @Transactional
    public User changePassword(User user, ChangePasswordRequest request) {
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("La contraseña actual no es correcta");
        }
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        return userRepository.save(user);
    }

    private AuthTokens tokensFor(User user) {
        return new AuthTokens(jwtService.generateAccessToken(user), jwtService.generateRefreshToken(user), user);
    }

    private String normalize(String email) { return email.trim().toLowerCase(java.util.Locale.ROOT); }

    public record AuthTokens(String accessToken, String refreshToken, User user) {
        public AuthResponse response(long expiresIn) {
            return AuthResponse.from(accessToken, expiresIn, user);
        }
    }
}
