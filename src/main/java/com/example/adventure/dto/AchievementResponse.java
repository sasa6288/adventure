package com.example.adventure.dto;

import com.example.adventure.model.AchievementType;
import java.time.LocalDateTime;

public record AchievementResponse(Long id, AchievementType type, String name,
                                  String description, boolean unlocked, LocalDateTime unlockedAt) {}
