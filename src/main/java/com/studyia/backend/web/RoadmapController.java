package com.studyia.backend.web;

import com.studyia.backend.service.RoadmapGenerationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import com.studyia.backend.model.User;
import com.studyia.backend.repository.UserRepository;

@RestController
@RequestMapping("/api/v1/roadmaps")
public class RoadmapController {

    private final RoadmapGenerationService roadmapGenerationService;
    private final UserRepository userRepository;

    public RoadmapController(RoadmapGenerationService roadmapGenerationService, UserRepository userRepository) {
        this.roadmapGenerationService = roadmapGenerationService;
        this.userRepository = userRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoadmapResponse generate(@Valid @RequestBody StudentProfileRequest request,
                                    @AuthenticationPrincipal UserDetails principal) {
        User user = userRepository.findByEmailIgnoreCase(principal.getUsername())
                .orElseThrow(() -> new IllegalStateException("Usuario autenticado no encontrado"));
        return roadmapGenerationService.generate(request, user);
    }
}
