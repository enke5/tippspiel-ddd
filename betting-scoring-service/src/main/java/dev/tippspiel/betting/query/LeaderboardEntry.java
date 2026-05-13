package dev.tippspiel.betting.query;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * A single row in the leaderboard of a betting group.
 * Rank is 1-based; ties share the same rank.
 */
public record LeaderboardEntry(
    int rank,
    UUID playerId,
    String displayName,
    int totalPoints,
    boolean stakePaid,
    boolean approved,
    BigDecimal prizeAmount   // null until prizes are distributed
) {}
