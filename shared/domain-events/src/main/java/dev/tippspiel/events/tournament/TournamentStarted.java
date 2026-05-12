package dev.tippspiel.events.tournament;

import java.time.Instant;
import java.util.UUID;

/**
 * Fired by an explicit admin command when the tournament officially starts.
 * Betting & Scoring uses this to lock all ChampionshipPredictions.
 * This is an intentional admin action — not derived from the first MatchStarted.
 */
public record TournamentStarted(
    UUID eventId,
    Instant occurredAt,
    String tournamentRef  // e.g. "UEFA_EURO_2028"
) implements TournamentManagementEvent {}
