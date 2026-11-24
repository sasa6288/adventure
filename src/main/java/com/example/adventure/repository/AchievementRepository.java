package com.example.adventure.repository;

import com.example.adventure.model.Achievement;
import com.example.adventure.model.AchievementType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AchievementRepository extends JpaRepository<Achievement, Long> {
    Optional<Achievement> findByType(AchievementType type);
}
