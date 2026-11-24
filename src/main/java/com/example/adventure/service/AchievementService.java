package com.example.adventure.service;

import com.example.adventure.dto.AchievementResponse;
import com.example.adventure.model.*;
import com.example.adventure.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class AchievementService {
    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;

    public AchievementService(AchievementRepository achievementRepository,
                              UserAchievementRepository userAchievementRepository) {
        this.achievementRepository = achievementRepository;
        this.userAchievementRepository = userAchievementRepository;
    }

    public void checkQuestAchievements(User user, long completedQuestCount) {
        unlockIf(user, AchievementType.FIRST_QUEST, completedQuestCount >= 1);
        unlockIf(user, AchievementType.QUEST_MASTER, completedQuestCount >= 10);
        checkLevelAchievement(user);
    }

    public void checkLevelAchievement(User user) {
        unlockIf(user, AchievementType.LEVEL_FIVE, user.getLevel() >= 5);
    }

    public void checkCheckInAchievement(User user, int streakDays) {
        unlockIf(user, AchievementType.SEVEN_DAY_STREAK, streakDays >= 7);
        checkLevelAchievement(user);
    }

    public List<AchievementResponse> findAll(Long userId) {
        Map<Long, UserAchievement> unlocked = new HashMap<>();
        userAchievementRepository.findAllByUserId(userId)
                .forEach(item -> unlocked.put(item.getAchievement().getId(), item));
        return achievementRepository.findAll().stream().map(a -> {
            UserAchievement ua = unlocked.get(a.getId());
            return new AchievementResponse(a.getId(), a.getType(), a.getName(), a.getDescription(),
                    ua != null, ua == null ? null : ua.getUnlockedAt());
        }).toList();
    }

    private void unlockIf(User user, AchievementType type, boolean condition) {
        if (!condition || userAchievementRepository.existsByUserIdAndType(user.getId(), type)) return;
        Achievement achievement = achievementRepository.findByType(type)
                .orElseThrow(() -> new IllegalStateException("成就配置不存在: " + type));
        userAchievementRepository.save(new UserAchievement(user, achievement, LocalDateTime.now()));
    }
}
