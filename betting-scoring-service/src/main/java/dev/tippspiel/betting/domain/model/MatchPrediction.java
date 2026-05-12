package dev.tippspiel.betting.domain.model;

import java.util.UUID;

/**
 * MatchPrediction aggregate root.
 *
 * Represents a player's predicted score for a single match within a BettingGroup.
 * Once locked (after MatchStarted), no further changes are accepted.
 */
public class MatchPrediction {

    public enum Status { OPEN, LOCKED, SCORED }

    private final UUID id;
    private final UUID playerId;
    private final UUID matchId;
    private final UUID bettingGroupId;
    private PredictedScore predictedScore;
    private Status status;
    private int pointsAwarded;

    public static MatchPrediction create(UUID id, UUID playerId, UUID matchId, UUID bettingGroupId) {
        return new MatchPrediction(id, playerId, matchId, bettingGroupId);
    }

    /** Reconstitution factory — restores a MatchPrediction from persisted state (infrastructure use only). */
    public static MatchPrediction reconstitute(UUID id, UUID playerId, UUID matchId,
                                               UUID bettingGroupId, PredictedScore predictedScore,
                                               Status status, int pointsAwarded) {
        MatchPrediction p = new MatchPrediction(id, playerId, matchId, bettingGroupId);
        p.predictedScore = predictedScore;
        p.status         = status;
        p.pointsAwarded  = pointsAwarded;
        return p;
    }

    private MatchPrediction(UUID id, UUID playerId, UUID matchId, UUID bettingGroupId) {
        this.id             = id;
        this.playerId       = playerId;
        this.matchId        = matchId;
        this.bettingGroupId = bettingGroupId;
        this.status         = Status.OPEN;
        this.pointsAwarded  = 0;
    }

    /** Submit or update a prediction. Only allowed while OPEN. */
    public void submit(PredictedScore score) {
        if (status != Status.OPEN) {
            throw new IllegalStateException("Prediction is " + status + " — cannot submit");
        }
        this.predictedScore = score;
    }

    /** Lock the prediction. Called when MatchStarted event is received. */
    public void lock() {
        if (status == Status.SCORED) return; // idempotent
        this.status = Status.LOCKED;
    }

    /** Score the prediction against the official result. */
    public void score(int points) {
        if (status != Status.LOCKED) {
            throw new IllegalStateException("Can only score a LOCKED prediction, current: " + status);
        }
        this.pointsAwarded = points;
        this.status        = Status.SCORED;
    }

    public UUID getId()               { return id; }
    public UUID getPlayerId()         { return playerId; }
    public UUID getMatchId()          { return matchId; }
    public UUID getBettingGroupId()   { return bettingGroupId; }
    public PredictedScore getPredictedScore() { return predictedScore; }
    public Status getStatus()         { return status; }
    public int getPointsAwarded()     { return pointsAwarded; }
    public boolean isOpen()           { return status == Status.OPEN; }
}
