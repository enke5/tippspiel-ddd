package dev.tippspiel.tournament.domain.model;

/** Immutable value object representing an official match result. */
public record Score(int goalsHome, int goalsAway) {

    public Score {
        if (goalsHome < 0 || goalsAway < 0) {
            throw new IllegalArgumentException("Goals cannot be negative");
        }
    }

    public Outcome outcome() {
        if (goalsHome > goalsAway)  return Outcome.HOME_WIN;
        if (goalsAway > goalsHome)  return Outcome.AWAY_WIN;
        return Outcome.DRAW;
    }

    public enum Outcome { HOME_WIN, DRAW, AWAY_WIN }
}
