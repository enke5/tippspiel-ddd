package dev.tippspiel.events.tournament;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Fired when the best third-place teams have been determined (UEFA-style:
 * the four best third-place finishers advance to the round of 16).
 * Betting & Scoring uses this to score championship predictions for best-third slots.
 */
public record BestThirdPlaceDetermined(
    UUID eventId,
    Instant occurredAt,
    List<EventTypes.TeamRef> qualifiedTeams  // the advancing third-place teams, ordered by ranking
) implements TournamentManagementEvent {}
