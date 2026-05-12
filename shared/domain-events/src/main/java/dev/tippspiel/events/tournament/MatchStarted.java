package dev.tippspiel.events.tournament;

import java.time.Instant;
import java.util.UUID;

/**
 * Fired when a match actually kicks off.
 * Betting & Scoring uses this to lock all MatchPredictions for this match,
 * and — if isFirstMatchOfGroup — to lock GroupBonusPredictions for this group.
 */
public record MatchStarted(
    UUID eventId,
    Instant occurredAt,
    UUID matchId,
    EventTypes.MatchRound round,
    String groupId,              // null for knockout rounds
    boolean isFirstMatchOfGroup  // true if this is the first match of the group stage group
) implements TournamentManagementEvent {}
