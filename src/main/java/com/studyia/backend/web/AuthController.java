package com.studyia.backend.web;

import com.studyia.backend.service.AuthService;
import com.studyia.backend.service.EmailVerificationService;
import com.studyia.backend.security.InvalidTokenException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseStatus;
import com.studyia.backend.web.ForgotPasswordRequest;
import com.studyia.backend.web.ResetPasswordRequest;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    static final String REFRESH_COOKIE = "studyia_refresh_token";

    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;
    private final boolean refreshCookieSecure;
    private final String refreshCookieSameSite;

    public AuthController(AuthService authService,
                          @Value("${studyia.security.access-token-expiration}") long accessTokenExpiration,
                          @Value("${studyia.security.refresh-token-expiration}") long refreshTokenExpiration,
                          @Value("${studyia.security.refresh-cookie-secure}") boolean refreshCookieSecure,
                          @Value("${studyia.security.refresh-cookie-same-site}") String refreshCookieSameSite,
                          EmailVerificationService emailVerificationService) {
        this.authService = authService;
        this.emailVerificationService = emailVerificationService;
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
        this.refreshCookieSecure = refreshCookieSecure;
        this.refreshCookieSameSite = refreshCookieSameSite;
    }

    @PostMapping("/register")
    public RegistrationResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/verify-email")
    public AuthResponse verifyEmail(@Valid @RequestBody VerifyEmailRequest request, HttpServletResponse response) {
        AuthService.AuthTokens tokens = authService.verifyEmail(request.email(), request.code());
        addRefreshCookie(response, tokens.refreshToken());
        return tokens.response(accessTokenExpiration);
    }

    @PostMapping("/resend-verification")
    public RegistrationResponse resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        emailVerificationService.resend(request.email());
        return new RegistrationResponse(request.email(), "Código de verificación reenviado");
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        AuthService.AuthTokens tokens = authService.login(request);
        addRefreshCookie(response, tokens.refreshToken());
        return tokens.response(accessTokenExpiration);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(
            @CookieValue(name = REFRESH_COOKIE, required = false) String refreshToken,
            HttpServletResponse response) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new InvalidTokenException("Refresh token requerido");
        }
        AuthService.AuthTokens tokens = authService.refresh(refreshToken);
        addRefreshCookie(response, tokens.refreshToken());
        return tokens.response(accessTokenExpiration);
    }

    @PostMapping("/forgot-password")
    public RegistrationResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.requestPasswordReset(request.email());
        return new RegistrationResponse(request.email(),
                "Si el correo está registrado, recibirás un enlace para restablecer tu contraseña");
    }

    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
    }

    @PostMapping("/logout")
    public void logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE, "")
                .httpOnly(true)
                .secure(refreshCookieSecure)
                .sameSite(refreshCookieSameSite)
                .path("/api/v1/auth")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void addRefreshCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_COOKIE, token)
                .httpOnly(true)
                .secure(refreshCookieSecure)
                .sameSite(refreshCookieSameSite)
                .path("/api/v1/auth")
                .maxAge(refreshTokenExpiration / 1000)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
