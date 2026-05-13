ALTER TABLE betting_group
    ADD COLUMN slug VARCHAR(50) NOT NULL DEFAULT '';

ALTER TABLE betting_group
    ADD CONSTRAINT uq_betting_group_slug UNIQUE (slug);
