package com.example.adventure.service;

import com.example.adventure.dto.CheckInResponse;
import com.example.adventure.exception.AlreadyCheckedInException;
import com.example.adventure.exception.UserNotFoundException;
import com.example.adventure.model.*;
import com.example.adventure.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;

@Service
public class CheckInService {
    private static final int EXPERIENCE_REWARD = 10;
    private static final int COIN_REWARD = 5;
    private final UserRepository userRepository;
    private final CheckInRepository checkInRepository;
    private final GameProgressService gameProgressService;
    private final AchievementService achievementService;

    public CheckInService(UserRepository userRepository, CheckInRepository checkInRepository,
                          GameProgressService gameProgressService, AchievementService achievementService) {
        this.userRepository = userRepository; this.checkInRepository = checkInRepository;
        this.gameProgressService = gameProgressService; this.achievementService = achievementService;
    }

    @Transactional
    public CheckInResponse checkIn(Long currentUserId) {
        User user = userRepository.findByIdForUpdate(currentUserId)
                .orElseThrow(() -> new UserNotFoundException(currentUserId));
        LocalDate today = LocalDate.now();
        if (checkInRepository.existsByUserIdAndCheckInDate(currentUserId, today)) {
            throw new AlreadyCheckedInException();
        }
        int streak = checkInRepository.findTopByUserIdOrderByCheckInDateDesc(currentUserId)
                .filter(last -> last.getCheckInDate().equals(today.minusDays(1)))
                .map(last -> last.getStreakDays() + 1).orElse(1);
        checkInRepository.save(new CheckIn(user, today, streak, LocalDateTime.now()));
        gameProgressService.addRewards(user, EXPERIENCE_REWARD, COIN_REWARD);
        achievementService.checkCheckInAchievement(user, streak);
        userRepository.save(user);
        return new CheckInResponse(today, streak, EXPERIENCE_REWARD, COIN_REWARD,
                user.getLevel(), user.getExperience(), user.getCoins());
    }
}
