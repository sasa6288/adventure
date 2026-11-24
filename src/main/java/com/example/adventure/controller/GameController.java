package com.example.adventure.controller;

import com.example.adventure.dto.*;
import com.example.adventure.security.AuthenticatedUser;
import com.example.adventure.service.*;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
public class GameController {
    private final CheckInService checkInService;
    private final AchievementService achievementService;
    private final RankingService rankingService;

    public GameController(CheckInService checkInService, AchievementService achievementService,
                          RankingService rankingService) {
        this.checkInService = checkInService; this.achievementService = achievementService;
        this.rankingService = rankingService;
    }

    @PostMapping("/api/check-ins")
    public ResponseEntity<CheckInResponse> checkIn(@AuthenticationPrincipal AuthenticatedUser principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(checkInService.checkIn(principal.user().getId()));
    }

    @GetMapping("/api/achievements")
    public List<AchievementResponse> achievements(@AuthenticationPrincipal AuthenticatedUser principal) {
        return achievementService.findAll(principal.user().getId());
    }

    @GetMapping("/api/rankings")
    public List<RankingResponse> rankings(@RequestParam(defaultValue = "20") int limit) {
        return rankingService.findRankings(limit);
    }
}
