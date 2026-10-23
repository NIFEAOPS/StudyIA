package com.studyia.backend.web;

import com.studyia.backend.model.User;
import com.studyia.backend.repository.UserRepository;
import com.studyia.backend.repository.RoadmapRepository;
import com.studyia.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/profile")
public class ProfileController {
    private final UserRepository userRepository;
    private final AuthService authService;
    private final RoadmapRepository roadmapRepository;

    public ProfileController(UserRepository userRepository, AuthService authService,
                             RoadmapRepository roadmapRepository) {
        this.userRepository = userRepository;
        this.authService = authService;
        this.roadmapRepository = roadmapRepository;
    }

    @GetMapping
    public ProfileResponse get(@AuthenticationPrincipal UserDetails principal) {
        return response(user(principal));
    }

    @PatchMapping
    public ProfileResponse update(@Valid @RequestBody UpdateProfileRequest request,
                                  @AuthenticationPrincipal UserDetails principal) {
        return response(authService.updateProfile(user(principal), request.university(), request.phone()));
    }

    @PatchMapping("/password")
    public ProfileResponse changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                          @AuthenticationPrincipal UserDetails principal) {
        return response(authService.changePassword(user(principal), request));
    }

    private User user(UserDetails principal) {
        return userRepository.findByEmailIgnoreCase(principal.getUsername())
                .orElseThrow(() -> new IllegalStateException("Usuario autenticado no encontrado"));
    }

    private ProfileResponse response(User user) {
        return ProfileResponse.from(user, roadmapRepository.countByStudentUserId(user.getId()));
    }
}
