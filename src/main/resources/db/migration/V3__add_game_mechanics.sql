CREATE TABLE achievements
(
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    type        VARCHAR(40)  NOT NULL,
    name        VARCHAR(80)  NOT NULL,
    description VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_achievements_type UNIQUE (type)
);

CREATE TABLE user_achievements
(
    id             BIGINT      NOT NULL AUTO_INCREMENT,
    user_id        BIGINT      NOT NULL,
    achievement_id BIGINT      NOT NULL,
    unlocked_at    DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_user_achievement UNIQUE (user_id, achievement_id),
    CONSTRAINT fk_user_achievements_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_achievements_achievement FOREIGN KEY (achievement_id) REFERENCES achievements (id)
);

CREATE TABLE check_ins
(
    id            BIGINT      NOT NULL AUTO_INCREMENT,
    user_id       BIGINT      NOT NULL,
    check_in_date DATE        NOT NULL,
    streak_days   INT         NOT NULL,
    created_at    DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_check_in_user_date UNIQUE (user_id, check_in_date),
    CONSTRAINT fk_check_ins_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_check_ins_user_date ON check_ins (user_id, check_in_date);
CREATE INDEX idx_user_achievements_user ON user_achievements (user_id);
CREATE INDEX idx_users_ranking ON users (level DESC, experience DESC, coins DESC);

INSERT INTO achievements (type, name, description) VALUES
('FIRST_QUEST', '初次冒险', '首次完成任务'),
('QUEST_MASTER', '任务达人', '累计完成10个任务'),
('LEVEL_FIVE', '渐入佳境', '角色等级达到5级'),
('SEVEN_DAY_STREAK', '持之以恒', '连续签到7天');
