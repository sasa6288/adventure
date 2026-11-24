package com.example.adventure.repository;

import com.example.adventure.model.UserAchievement;
import com.example.adventure.model.AchievementType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {
    @Query("SELECT ua FROM UserAchievement ua JOIN FETCH ua.achievement WHERE ua.user.id = :userId ORDER BY ua.unlockedAt")
    List<UserAchievement> findAllByUserId(@Param("userId") Long userId);
    @Query("SELECT CASE WHEN COUNT(ua) > 0 THEN true ELSE false END FROM UserAchievement ua WHERE ua.user.id = :userId AND ua.achievement.type = :type")
    boolean existsByUserIdAndType(@Param("userId") Long userId, @Param("type") AchievementType type);
}
