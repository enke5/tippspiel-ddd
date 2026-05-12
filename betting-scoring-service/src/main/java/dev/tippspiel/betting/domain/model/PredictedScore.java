package dev.tippspiel.betting.domain.model;

/** Value Object: a player's predicted score for a match. */
public record PredictedScore(int goalsHome, int goalsAway) {

    public PredictedScore {
        if (goalsHome < 0 || goalsAway < 0) {
            throw new IllegalArgumentException("Predicted goals cannot be negative");
        }
    }

    public Outcome predictedOutcome() {
        if (goalsHome > goalsAway)  return Outcome.HOME_WIN;
        if (goalsAway > goalsHome)  return Outcome.AWAY_WIN;
        return Outcome.DRAW;
    }

    public enum Outcome { HOME_WIN, DRAW, AWAY_WIN }
}
