CREATE TABLE betting_group (
    id              UUID         PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    tournament_ref  VARCHAR(50)  NOT NULL,
    stake_amount    NUMERIC(8,2) NOT NULL,
    stake_currency  CHAR(3)      NOT NULL DEFAULT 'EUR',
    status          VARCHAR(10)  NOT NULL DEFAULT 'OPEN',
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE participation (
    id              UUID         PRIMARY KEY,
    betting_group_id UUID        NOT NULL REFERENCES betting_group(id),
    player_id       UUID         NOT NULL,
    display_name    VARCHAR(100) NOT NULL,
    approved        BOOLEAN      NOT NULL DEFAULT FALSE,
    stake_paid      BOOLEAN      NOT NULL DEFAULT FALSE,
    total_points    INT          NOT NULL DEFAULT 0,
    prize_amount    NUMERIC(8,2),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    UNIQUE (betting_group_id, player_id)
);

CREATE TABLE match_prediction (
    id                  UUID        PRIMARY KEY,
    player_id           UUID        NOT NULL,
    match_id            UUID        NOT NULL,
    betting_group_id    UUID        NOT NULL REFERENCES betting_group(id),
    group_id            VARCHAR(10),
    predicted_goals_home SMALLINT,
    predicted_goals_away SMALLINT,
    status              VARCHAR(10) NOT NULL DEFAULT 'OPEN',
    points_awarded      INT         NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (player_id, match_id, betting_group_id)
);

CREATE INDEX match_prediction_match_idx        ON match_prediction (match_id);
CREATE INDEX match_prediction_group_idx        ON match_prediction (betting_group_id);
CREATE INDEX match_prediction_tournament_group ON match_prediction (group_id);

CREATE TABLE group_bonus_prediction (
    id                   UUID        PRIMARY KEY,
    player_id            UUID        NOT NULL,
    tournament_group_id  VARCHAR(10) NOT NULL,
    betting_group_id     UUID        NOT NULL REFERENCES betting_group(id),
    predicted_first_team VARCHAR(10),
    predicted_second_team VARCHAR(10),
    status               VARCHAR(10) NOT NULL DEFAULT 'OPEN',
    points_awarded       INT         NOT NULL DEFAULT 0,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (player_id, tournament_group_id, betting_group_id)
);

CREATE TABLE championship_prediction (
    id                   UUID        PRIMARY KEY,
    player_id            UUID        NOT NULL,
    betting_group_id     UUID        NOT NULL REFERENCES betting_group(id),
    semifinalist_1       VARCHAR(10),
    semifinalist_2       VARCHAR(10),
    semifinalist_3       VARCHAR(10),
    semifinalist_4       VARCHAR(10),
    champion             VARCHAR(10),
    status               VARCHAR(10) NOT NULL DEFAULT 'OPEN',
    points_awarded       INT         NOT NULL DEFAULT 0,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (player_id, betting_group_id)
);
