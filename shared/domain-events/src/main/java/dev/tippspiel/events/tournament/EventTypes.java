package dev.tippspiel.events.tournament;

import java.time.Instant;
import java.util.UUID;

/**
 * Shared value types used in tournament domain events.
 */
public final class EventTypes {

    private EventTypes() {}

    /** ISO 3166-1 alpha-2 team code, e.g. "DE", "FR". */
    public record TeamRef(String teamId, String name) {}

    /** Score at a given point in time. */
    public record Score(int goalsHome, int goalsAway) {}

    /** A tournament group, e.g. "A", "B", ... */
    public record GroupRef(String groupId, String label) {}

    /** A team's standing entry within its group. */
    public record StandingEntry(int rank, TeamRef team, int points, int goalsFor, int goalsAgainst) {}

    public enum MatchRound {
        GROUP_STAGE,
        ROUND_OF_16,
        QUARTER_FINAL,
        SEMI_FINAL,
        THIRD_PLACE,
        FINAL
    }

    public enum MatchPhase {
        REGULAR_TIME,
        EXTRA_TIME,
        PENALTY_SHOOTOUT
    }

    public enum Side {
        HOME,
        AWAY
    }
}
