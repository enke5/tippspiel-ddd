CREATE TABLE tournament_group (
    id                    UUID         PRIMARY KEY,
    label                 VARCHAR(20)  NOT NULL,
    tournament_ref        VARCHAR(50)  NOT NULL,
    standings_policy_name VARCHAR(100) NOT NULL,
    teams                 JSONB        NOT NULL,
    status                VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    final_standings       JSONB,
    created_at            TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX tournament_group_tournament_ref_idx ON tournament_group (tournament_ref);
