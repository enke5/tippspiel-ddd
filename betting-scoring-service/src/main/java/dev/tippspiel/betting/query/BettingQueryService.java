package dev.tippspiel.betting.query;

import java.util.List;
import java.util.UUID;

/**
 * Read-side queries for the betting-scoring service.
 *
 * Implementations may use any read strategy (JPA projections, native SQL, etc.)
 * without touching the write-side domain model.
 */
public interface BettingQueryService {

    /**
     * Returns the leaderboard for a betting group, sorted by points descending.
     * Ties receive the same rank; the next rank after a tie block is
     * (previous rank + tie block size), i.e. 1, 2, 2, 4, …
     */
    List<LeaderboardEntry> getLeaderboard(UUID bettingGroupId);

    /**
     * Returns all match predictions submitted by a player inside a betting group.
     * Only predictions that were explicitly submitted (not just auto-created) are returned.
     */
    List<PredictionStatusEntry> getPredictionStatus(UUID bettingGroupId, UUID playerId);
}
