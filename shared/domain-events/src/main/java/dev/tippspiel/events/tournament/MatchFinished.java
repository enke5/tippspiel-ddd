package dev.tippspiel.events.tournament;

import java.time.Instant;
import java.util.UUID;

/**
 * Fired when the official result of a match is recorded.
 * Betting & Scoring uses this to score all MatchPredictions for this match.
 * For SEMI_FINAL and FINAL rounds, additional championship prediction scoring
 * is triggered.
 */
public record MatchFinished(
    UUID eventId,
    Instant occurredAt,
    UUID matchId,
    EventTypes.MatchRound round,
    String groupId,              // null for knockout rounds
    EventTypes.TeamRef homeTeam,
    EventTypes.TeamRef awayTeam,
    EventTypes.Score score,
    EventTypes.MatchPhase phase  // how the match was decided (regular / extra / penalties)
) implements TournamentManagementEvent {}
