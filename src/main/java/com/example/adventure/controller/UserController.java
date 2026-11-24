package com.example.adventure.controller;

import com.example.adventure.dto.*;
import com.example.adventure.security.AuthenticatedUser;
import com.example.adventure.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/me")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) { this.userService = userService; }

    @GetMapping
    public ResponseEntity<UserResponse> getMe(@AuthenticationPrincipal AuthenticatedUser principal) {
        return ResponseEntity.ok(userService.getMe(principal.user()));
    }

    @PutMapping
    public ResponseEntity<UserResponse> updateMe(@AuthenticationPrincipal AuthenticatedUser principal,
                                                  @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateMe(principal.user(), request));
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal AuthenticatedUser principal,
                                                @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(principal.user(), request);
        return ResponseEntity.noContent().build();
    }
}
