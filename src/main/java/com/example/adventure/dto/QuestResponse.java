package com.example.adventure.dto;

import com.example.adventure.model.QuestDifficulty;
import com.example.adventure.model.QuestStatus;
import com.example.adventure.model.QuestType;

import java.time.LocalDateTime;

public record QuestResponse(
        Long id,
        String title,
        String description,
        QuestType type,
        QuestDifficulty difficulty,
        QuestStatus status,
        Integer rewardExperience,
        Integer rewardCoins,
        LocalDateTime deadline,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
