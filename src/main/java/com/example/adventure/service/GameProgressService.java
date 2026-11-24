package com.example.adventure.service;

import com.example.adventure.model.User;
import org.springframework.stereotype.Service;

@Service
public class GameProgressService {
    public void addRewards(User user, int experience, int coins) {
        user.setExperience(user.getExperience() + experience);
        user.setCoins(user.getCoins() + coins);
        while (user.getExperience() >= experienceRequiredForLevel(user.getLevel() + 1)) {
            user.setLevel(user.getLevel() + 1);
        }
    }

    // 升到 N 级所需累计经验：100 + 200 + ... + (N-1)*100
    private int experienceRequiredForLevel(int level) {
        return 50 * level * (level - 1);
    }
}
