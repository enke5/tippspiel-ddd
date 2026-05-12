package dev.tippspiel.tournament.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * TournamentGroup aggregate root.
 *
 * Tracks the teams in a group stage group and the name of the
 * StandingsPolicy to apply when computing the final table.
 *
 * Lifecycle: ACTIVE → WAITING_FOR_LOTS_DRAW (if lots needed) → COMPLETED
 */
public class TournamentGroup {

    public enum Status { ACTIVE, WAITING_FOR_LOTS_DRAW, COMPLETED }

    private final UUID id;
    private final String label;               // e.g. "A", "B", "Group of Death"
    private final String tournamentRef;
    private final String standingsPolicyName; // references a policy in standings-policies.json
    private final List<TeamRef> teams;
    private Status status;
    private List<TeamRef> finalStandings;     // null until COMPLETED

    public static TournamentGroup create(UUID id, String label, String tournamentRef,
                                         String standingsPolicyName, List<TeamRef> teams) {
        if (label == null || label.isBlank()) throw new IllegalArgumentException("Group label required");
        if (standingsPolicyName == null || standingsPolicyName.isBlank())
            throw new IllegalArgumentException("standingsPolicyName required");
        if (teams == null || teams.size() < 2)
            throw new IllegalArgumentException("At least 2 teams required");
        return new TournamentGroup(id, label, tournamentRef, standingsPolicyName, teams);
    }

    /** Reconstitution factory — restores a TournamentGroup from persisted state (infrastructure use only). */
    public static TournamentGroup reconstitute(UUID id, String label, String tournamentRef,
                                               String standingsPolicyName, List<TeamRef> teams,
                                               Status status, List<TeamRef> finalStandings) {
        TournamentGroup g = new TournamentGroup(id, label, tournamentRef, standingsPolicyName, teams);
        g.status         = status;
        g.finalStandings = (finalStandings != null && !finalStandings.isEmpty())
                ? new ArrayList<>(finalStandings)
                : null;
        return g;
    }

    private TournamentGroup(UUID id, String label, String tournamentRef,
                             String standingsPolicyName, List<TeamRef> teams) {
        this.id                  = id;
        this.label               = label;
        this.tournamentRef       = tournamentRef;
        this.standingsPolicyName = standingsPolicyName;
        this.teams               = List.copyOf(teams);
        this.status              = Status.ACTIVE;
    }

    /**
     * Record the final standings after all group matches have finished.
     * If the standings contain a tie that requires a draw, the status
     * transitions to WAITING_FOR_LOTS_DRAW instead of COMPLETED.
     *
     * @param orderedTeams teams ordered 1st to last
     * @param requiresLotsDraw true if the calculator returned a DRAWING_OF_LOTS tie
     */
    public void recordStandings(List<TeamRef> orderedTeams, boolean requiresLotsDraw) {
        if (status == Status.COMPLETED) throw new IllegalStateException("Group already completed");
        this.finalStandings = new ArrayList<>(orderedTeams);
        this.status = requiresLotsDraw ? Status.WAITING_FOR_LOTS_DRAW : Status.COMPLETED;
    }

    /**
     * Confirm standings after a lots draw has been conducted by tournament officials.
     * Moves from WAITING_FOR_LOTS_DRAW → COMPLETED.
     */
    public void confirmLotsDrawResult(List<TeamRef> confirmedStandings) {
        if (status != Status.WAITING_FOR_LOTS_DRAW) {
            throw new IllegalStateException("Group is not waiting for a lots draw, current status: " + status);
        }
        this.finalStandings = new ArrayList<>(confirmedStandings);
        this.status = Status.COMPLETED;
    }

    // --- Getters ---

    public UUID getId()                  { return id; }
    public String getLabel()             { return label; }
    public String getTournamentRef()     { return tournamentRef; }
    public String getStandingsPolicyName() { return standingsPolicyName; }
    public List<TeamRef> getTeams()      { return teams; }
    public Status getStatus()            { return status; }

    public List<TeamRef> getFinalStandings() {
        if (finalStandings == null) return Collections.emptyList();
        return Collections.unmodifiableList(finalStandings);
    }

    public boolean isCompleted() { return status == Status.COMPLETED; }
}
