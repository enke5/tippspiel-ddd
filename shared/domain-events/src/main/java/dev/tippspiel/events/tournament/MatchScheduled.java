package dev.tippspiel.events.tournament;

import java.time.Instant;
import java.util.UUID;

public record MatchScheduled(
    UUID eventId,
    Instant occurredAt,
    UUID matchId,
    EventTypes.MatchRound round,
    String groupId,            // null for knockout rounds
    String homePlaceholder,
    String awayPlaceholder,
    Instant scheduledFor
) implements TournamentManagementEvent {}
