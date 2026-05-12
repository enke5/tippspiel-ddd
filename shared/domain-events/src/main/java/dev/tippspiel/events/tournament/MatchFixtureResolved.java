package dev.tippspiel.events.tournament;

import java.time.Instant;
import java.util.UUID;

/**
 * Fired when a placeholder team slot in a knockout match is resolved to an
 * actual team (e.g. "Winner Group A" → Germany).
 * Betting & Scoring uses this to replace placeholders in existing MatchPredictions.
 */
public record MatchFixtureResolved(
    UUID eventId,
    Instant occurredAt,
    UUID matchId,
    String placeholder,
    EventTypes.TeamRef team,
    EventTypes.Side side
) implements TournamentManagementEvent {}
