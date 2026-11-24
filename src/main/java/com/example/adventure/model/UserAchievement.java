package com.example.adventure.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_achievements", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "achievement_id"}))
public class UserAchievement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "achievement_id", nullable = false)
    private Achievement achievement;
    @Column(name = "unlocked_at", nullable = false)
    private LocalDateTime unlockedAt;

    public UserAchievement() {}
    public UserAchievement(User user, Achievement achievement, LocalDateTime unlockedAt) {
        this.user = user;
        this.achievement = achievement;
        this.unlockedAt = unlockedAt;
    }
    public Achievement getAchievement() { return achievement; }
    public LocalDateTime getUnlockedAt() { return unlockedAt; }
}
