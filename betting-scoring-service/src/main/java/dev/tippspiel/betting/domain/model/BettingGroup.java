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
    private final String slug;         // URL-friendly identifier, e.g. "arbeit" or "privat"
    private final Stake stake;
    private final String tournamentRef;
    private Status status;

    public static BettingGroup create(UUID id, String name, String slug, Stake stake, String tournamentRef) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Group name required");
        if (slug == null || !slug.matches("[a-z0-9-]+")) throw new IllegalArgumentException("Slug must be lowercase alphanumeric with hyphens");
        return new BettingGroup(id, name, slug, stake, tournamentRef, Status.OPEN);
    }

    /** Reconstitution factory — restores a BettingGroup from persisted state (infrastructure use only). */
    public static BettingGroup reconstitute(UUID id, String name, String slug, Stake stake,
                                            String tournamentRef, Status status) {
        return new BettingGroup(id, name, slug, stake, tournamentRef, status);
    }

    private BettingGroup(UUID id, String name, String slug, Stake stake, String tournamentRef, Status status) {
        this.id            = id;
        this.name          = name;
        this.slug          = slug;
        this.stake         = stake;
        this.tournamentRef = tournamentRef;
        this.status        = status;
    }

    public void close() {
        this.status = Status.CLOSED;
    }

    public UUID getId()              { return id; }
    public String getName()          { return name; }
    public String getSlug()          { return slug; }
    public Stake getStake()          { return stake; }
    public String getTournamentRef() { return tournamentRef; }
    public Status getStatus()        { return status; }
    public boolean isOpen()          { return status == Status.OPEN; }
}
