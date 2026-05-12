package dev.tippspiel.tournament.domain.service;

import dev.tippspiel.tournament.domain.model.Score;
import dev.tippspiel.tournament.domain.model.TeamRef;

/**
 * Value Object: the result of a single group stage match,
 * used as input for GroupStandingsCalculator.
 *
 * Only finished matches should be passed to the calculator.
 */
public record GroupMatch(
    TeamRef homeTeam,
    TeamRef awayTeam,
    Score   score
) {

    public GroupMatch {
        if (homeTeam == null) throw new IllegalArgumentException("homeTeam required");
        if (awayTeam == null) throw new IllegalArgumentException("awayTeam required");
        if (score    == null) throw new IllegalArgumentException("score required");
    }

    public TeamRef winner() {
        return switch (score.outcome()) {
            case HOME_WIN -> homeTeam;
            case AWAY_WIN -> awayTeam;
            case DRAW     -> null;
        };
    }

    public boolean isDraw() { return score.outcome() == Score.Outcome.DRAW; }

    /** Points earned by the home team in this match. */
    public int homePoints() {
        return switch (score.outcome()) {
            case HOME_WIN -> 3;
            case DRAW     -> 1;
            case AWAY_WIN -> 0;
        };
    }

    /** Points earned by the away team in this match. */
    public int awayPoints() {
        return switch (score.outcome()) {
            case AWAY_WIN -> 3;
            case DRAW     -> 1;
            case HOME_WIN -> 0;
        };
    }
}
