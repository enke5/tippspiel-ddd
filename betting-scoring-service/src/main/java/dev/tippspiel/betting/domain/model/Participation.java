package dev.tippspiel.betting.domain.model;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * A player's participation in a BettingGroup.
 * Tracks accumulated points and the prize amount awarded at settlement.
 */
public class Participation {

    private final UUID   id;
    private final UUID   bettingGroupId;
    private final UUID   playerId;
    private final String displayName;
    private       int    totalPoints;
    private       boolean stakePaid;
    private       boolean approved;
    private       BigDecimal prizeAmount;

    public static Participation create(UUID id, UUID bettingGroupId, UUID playerId, String displayName) {
        if (displayName == null || displayName.isBlank()) throw new IllegalArgumentException("displayName required");
        return new Participation(id, bettingGroupId, playerId, displayName, 0, false, false, null);
    }

    public static Participation reconstitute(UUID id, UUID bettingGroupId, UUID playerId,
                                             String displayName, int totalPoints,
                                             boolean stakePaid, boolean approved,
                                             BigDecimal prizeAmount) {
        return new Participation(id, bettingGroupId, playerId, displayName,
                                 totalPoints, stakePaid, approved, prizeAmount);
    }

    private Participation(UUID id, UUID bettingGroupId, UUID playerId, String displayName,
                          int totalPoints, boolean stakePaid, boolean approved, BigDecimal prizeAmount) {
        this.id             = id;
        this.bettingGroupId = bettingGroupId;
        this.playerId       = playerId;
        this.displayName    = displayName;
        this.totalPoints    = totalPoints;
        this.stakePaid      = stakePaid;
        this.approved       = approved;
        this.prizeAmount    = prizeAmount;
    }

    public void addPoints(int points) {
        this.totalPoints += points;
    }

    public void awardPrize(BigDecimal amount) {
        this.prizeAmount = amount;
    }

    public UUID getId()               { return id; }
    public UUID getBettingGroupId()   { return bettingGroupId; }
    public UUID getPlayerId()         { return playerId; }
    public String getDisplayName()    { return displayName; }
    public int getTotalPoints()       { return totalPoints; }
    public boolean isStakePaid()      { return stakePaid; }
    public boolean isApproved()       { return approved; }
    public BigDecimal getPrizeAmount(){ return prizeAmount; }
}
