package dev.tippspiel.betting.domain.model;

import java.util.UUID;

/**
 * BettingGroup aggregate root.
 *
 * A named group of participants who bet on the same tournament together.
 * The stake is fixed per group and applies to all participants.
 */
public class BettingGroup {

    public enum Status { OPEN, CLOSED }

    private final UUID id;
    private final String name;
    private final Stake stake;
    private final String tournamentRef;
    private Status status;

    public static BettingGroup create(UUID id, String name, Stake stake, String tournamentRef) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Group name required");
        return new BettingGroup(id, name, stake, tournamentRef);
    }

    private BettingGroup(UUID id, String name, Stake stake, String tournamentRef) {
        this.id            = id;
        this.name          = name;
        this.stake         = stake;
        this.tournamentRef = tournamentRef;
        this.status        = Status.OPEN;
    }

    public void close() {
        this.status = Status.CLOSED;
    }

    public UUID getId()           { return id; }
    public String getName()       { return name; }
    public Stake getStake()       { return stake; }
    public String getTournamentRef() { return tournamentRef; }
    public Status getStatus()     { return status; }
    public boolean isOpen()       { return status == Status.OPEN; }
}
