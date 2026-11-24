CREATE TABLE quests
(
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    title          VARCHAR(100) NOT NULL,
    description    VARCHAR(1000),
    type           VARCHAR(30)  NOT NULL,
    difficulty     VARCHAR(30)  NOT NULL,
    status         VARCHAR(30)  NOT NULL,
    reward_exp     INT          NOT NULL,
    reward_coin    INT          NOT NULL,
    deadline       DATETIME(6),
    created_at     DATETIME(6)  NOT NULL,
    updated_at     DATETIME(6)  NOT NULL,

    PRIMARY KEY (id)
);

CREATE INDEX idx_quests_status
    ON quests (status);

CREATE INDEX idx_quests_created_at
    ON quests (created_at);

CREATE INDEX idx_quests_title
    ON quests (title);
