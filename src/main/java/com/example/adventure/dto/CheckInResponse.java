package com.example.adventure.dto;

import java.time.LocalDate;

public record CheckInResponse(LocalDate date, int streakDays, int experienceReward,
                              int coinReward, int level, int experience, int coins) {}
