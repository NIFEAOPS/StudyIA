package com.studyia.backend.web;

import com.studyia.backend.model.User;
import com.studyia.backend.repository.UserRepository;
import com.studyia.backend.service.ProgressService;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class DashboardController {

    private final ProgressService progressService;
    private final UserRepository userRepository;

    public DashboardController(ProgressService progressService, UserRepository userRepository) {
        this.progressService = progressService;
        this.userRepository = userRepository;
    }

    @GetMapping("/dashboard")
    public DashboardResponse dashboard(@AuthenticationPrincipal UserDetails principal) {
        return progressService.dashboard(user(principal));
    }

    @PatchMapping("/activities/{activityId}/complete")
    public DashboardResponse complete(@PathVariable UUID activityId,
                                      @AuthenticationPrincipal UserDetails principal) {
        return progressService.completeActivity(user(principal), activityId);
    }

    private User user(UserDetails principal) {
        return userRepository.findByEmailIgnoreCase(principal.getUsername())
                .orElseThrow(() -> new IllegalStateException("Usuario autenticado no encontrado"));
    }
}
