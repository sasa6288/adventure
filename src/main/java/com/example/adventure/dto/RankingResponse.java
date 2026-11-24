package com.example.adventure.dto;

public record RankingResponse(int rank, Long userId, String username, String nickname,
                              int level, int experience, int coins) {}
