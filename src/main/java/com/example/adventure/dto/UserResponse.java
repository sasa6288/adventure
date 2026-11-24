package com.example.adventure.dto;

import com.example.adventure.model.UserRole;

public record UserResponse(
        Long id,
        String username,
        String nickname,
        UserRole role,
        boolean enabled,
        int level,
        int experience,
        int coins
) {
}
