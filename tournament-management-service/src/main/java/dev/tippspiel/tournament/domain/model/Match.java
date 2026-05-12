package dev.tippspiel.tournament.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Match aggregate root.
 *
 * Lifecycle: SCHEDULED → STARTED → FINISHED
 *                      ↘ FIXTURE_RESOLVED (knockout only, before STARTED)
 */
public class Match {

    public enum Status { SCHEDULED, FIXTURE_RESOLVED, STARTED, FINISHED }

    private final UUID id;
    private final MatchRound round;
    private final String groupId;           // null for knockout rounds
    private String homePlaceholder;
    private String awayPlaceholder;
    private TeamRef homeTeam;               // null until fixture resolved
    private TeamRef awayTeam;               // null until fixture resolved
    private final Instant scheduledFor;
    private Status status;
    private Score score;                    // null until FINISHED
    private MatchPhase phase;              // null until FINISHED

    /** Factory — creates a scheduled match with placeholder team names. */
    public static Match schedule(UUID id, MatchRound round, String groupId,
                                 String homePlaceholder, String awayPlaceholder,
                                 Instant scheduledFor) {
        return new Match(id, round, groupId, homePlaceholder, awayPlaceholder, scheduledFor);
    }

    /** Reconstitution factory — restores a Match from persisted state (infrastructure use only). */
    public static Match reconstitute(UUID id, MatchRound round, String groupId,
                                     String homePlaceholder, String awayPlaceholder,
                                     TeamRef homeTeam, TeamRef awayTeam,
                                     Instant scheduledFor, Status status,
                                     Score score, MatchPhase phase) {
        Match m = new Match(id, round, groupId, homePlaceholder, awayPlaceholder, scheduledFor);
        m.homeTeam = homeTeam;
        m.awayTeam = awayTeam;
        m.status   = status;
        m.score    = score;
        m.phase    = phase;
        return m;
    }

    private Match(UUID id, MatchRound round, String groupId,
                  String homePlaceholder, String awayPlaceholder, Instant scheduledFor) {
        this.id              = id;
        this.round           = round;
        this.groupId         = groupId;
        this.homePlaceholder = homePlaceholder;
        this.awayPlaceholder = awayPlaceholder;
        this.scheduledFor    = scheduledFor;
        this.status          = Status.SCHEDULED;
    }

    /** Resolves a placeholder to a real team (knockout rounds only). */
    public void resolveFixture(String placeholder, TeamRef team, Side side) {
        if (status == Status.STARTED || status == Status.FINISHED) {
            throw new IllegalStateException("Cannot resolve fixture of a match in status " + status);
        }
        if (side == Side.HOME) {
            this.homeTeam = team;
            this.homePlaceholder = null;
        } else {
            this.awayTeam = team;
            this.awayPlaceholder = null;
        }
        if (homeTeam != null && awayTeam != null) {
            this.status = Status.FIXTURE_RESOLVED;
        }
    }

    /** Transitions the match to STARTED. Tipping window closes as a consequence. */
    public void start() {
        if (status != Status.SCHEDULED && status != Status.FIXTURE_RESOLVED) {
            throw new IllegalStateException("Match cannot be started in status " + status);
        }
        this.status = Status.STARTED;
    }

    /** Records the official result. */
    public void finish(Score score, MatchPhase phase) {
        if (status != Status.STARTED) {
            throw new IllegalStateException("Match cannot be finished in status " + status);
        }
        this.score  = score;
        this.phase  = phase;
        this.status = Status.FINISHED;
    }

    // --- Getters (no setters — all state changes go through domain methods) ---

    public UUID getId()                 { return id; }
    public MatchRound getRound()        { return round; }
    public String getGroupId()          { return groupId; }
    public String getHomePlaceholder()  { return homePlaceholder; }
    public String getAwayPlaceholder()  { return awayPlaceholder; }
    public TeamRef getHomeTeam()        { return homeTeam; }
    public TeamRef getAwayTeam()        { return awayTeam; }
    public Instant getScheduledFor()    { return scheduledFor; }
    public Status getStatus()           { return status; }
    public Score getScore()             { return score; }
    public MatchPhase getPhase()        { return phase; }

    public boolean isGroupStageMatch()  { return round == MatchRound.GROUP_STAGE; }
}
