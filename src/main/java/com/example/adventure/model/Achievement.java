package com.example.adventure.model;

import jakarta.persistence.*;

@Entity
@Table(name = "achievements")
public class Achievement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 40)
    private AchievementType type;
    @Column(nullable = false, length = 80)
    private String name;
    @Column(nullable = false, length = 255)
    private String description;

    public Achievement() {}

    public Achievement(AchievementType type, String name, String description) {
        this.type = type;
        this.name = name;
        this.description = description;
    }

    public Long getId() { return id; }
    public AchievementType getType() { return type; }
    public String getName() { return name; }
    public String getDescription() { return description; }
}
