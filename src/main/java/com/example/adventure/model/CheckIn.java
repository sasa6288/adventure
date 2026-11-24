package com.example.adventure.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "check_ins", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "check_in_date"}))
public class CheckIn {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;
    @Column(name = "streak_days", nullable = false)
    private int streakDays;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public CheckIn() {}
    public CheckIn(User user, LocalDate date, int streakDays, LocalDateTime createdAt) {
        this.user = user; this.checkInDate = date; this.streakDays = streakDays; this.createdAt = createdAt;
    }
    public LocalDate getCheckInDate() { return checkInDate; }
    public int getStreakDays() { return streakDays; }
}
