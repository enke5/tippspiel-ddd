package dev.tippspiel.events.tournament;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Fired when all matches in a tournament group stage group have finished.
 * Carries the final standings so Betting & Scoring can score group bonus predictions
 * and semi-finalist championship predictions.
 */
public record TournamentGroupCompleted(
    UUID eventId,
    Instant occurredAt,
    EventTypes.GroupRef group,
    List<EventTypes.StandingEntry> finalStandings  // ordered rank 1..N
) implements TournamentManagementEvent {}
