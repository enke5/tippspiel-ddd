package dev.tippspiel.betting.query;

import java.util.UUID;

/**
 * Status of a single match prediction for a player within a betting group.
 *
 * status values mirror MatchPrediction domain states: OPEN | LOCKED | SCORED
 */
public record PredictionStatusEntry(
    UUID predictionId,
    UUID matchId,
    Integer predictedGoalsHome,  // null when no prediction submitted yet
    Integer predictedGoalsAway,
    String status,
    int pointsAwarded
) {}
