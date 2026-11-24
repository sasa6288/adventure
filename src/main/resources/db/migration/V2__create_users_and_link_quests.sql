CREATE TABLE users
(
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    username   VARCHAR(50)  NOT NULL,
    password   VARCHAR(100) NOT NULL,
    nickname   VARCHAR(50)  NOT NULL,
    role       VARCHAR(20)  NOT NULL,
    enabled    BOOLEAN      NOT NULL DEFAULT TRUE,
    level      INT          NOT NULL DEFAULT 1,
    experience INT          NOT NULL DEFAULT 0,
    coins      INT          NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_users_username UNIQUE (username)
);

ALTER TABLE quests
    ADD COLUMN user_id BIGINT NULL;

ALTER TABLE quests
    ADD CONSTRAINT fk_quests_user
        FOREIGN KEY (user_id) REFERENCES users (id);

CREATE INDEX idx_quests_user_id ON quests (user_id);
