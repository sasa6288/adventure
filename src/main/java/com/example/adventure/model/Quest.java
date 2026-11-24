package com.example.adventure.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "quests")
public class Quest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private QuestType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private QuestDifficulty difficulty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private QuestStatus status;

    @Column(name = "reward_exp", nullable = false)
    private Integer rewardExperience;

    @Column(name = "reward_coin", nullable = false)
    private Integer rewardCoins;

    @Column(name = "deadline")
    private LocalDateTime deadline;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Quest() {
    }

    public Quest(Long id, String title, String description, QuestType type,
                 QuestDifficulty difficulty, QuestStatus status,
                 Integer rewardExperience, Integer rewardCoins,
                 LocalDateTime deadline, LocalDateTime createdAt,
                 LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.type = type;
        this.difficulty = difficulty;
        this.status = status;
        this.rewardExperience = rewardExperience;
        this.rewardCoins = rewardCoins;
        this.deadline = deadline;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public QuestType getType() { return type; }
    public void setType(QuestType type) { this.type = type; }
    public QuestDifficulty getDifficulty() { return difficulty; }
    public void setDifficulty(QuestDifficulty difficulty) { this.difficulty = difficulty; }
    public QuestStatus getStatus() { return status; }
    public void setStatus(QuestStatus status) { this.status = status; }
    public Integer getRewardExperience() { return rewardExperience; }
    public void setRewardExperience(Integer rewardExperience) { this.rewardExperience = rewardExperience; }
    public Integer getRewardCoins() { return rewardCoins; }
    public void setRewardCoins(Integer rewardCoins) { this.rewardCoins = rewardCoins; }
    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
