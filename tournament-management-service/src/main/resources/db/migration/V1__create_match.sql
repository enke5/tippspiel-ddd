CREATE TABLE match (
    id               UUID PRIMARY KEY,
    round            VARCHAR(20)  NOT NULL,
    group_id         VARCHAR(10),
    home_placeholder VARCHAR(100),
    away_placeholder VARCHAR(100),
    home_team_id     VARCHAR(10),
    home_team_name   VARCHAR(100),
    away_team_id     VARCHAR(10),
    away_team_name   VARCHAR(100),
    scheduled_for    TIMESTAMPTZ  NOT NULL,
    status           VARCHAR(20)  NOT NULL DEFAULT 'SCHEDULED',
    goals_home       SMALLINT,
    goals_away       SMALLINT,
    phase            VARCHAR(25),
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX match_group_id_idx ON match (group_id);
CREATE INDEX match_status_idx   ON match (status);
