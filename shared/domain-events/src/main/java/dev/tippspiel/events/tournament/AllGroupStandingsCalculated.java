package dev.tippspiel.events.tournament;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Fired when all group stage groups have been completed and the full group
 * stage table is available. Published after all TournamentGroupCompleted events.
 */
public record AllGroupStandingsCalculated(
    UUID eventId,
    Instant occurredAt,
    List<EventTypes.GroupRef> completedGroups
) implements TournamentManagementEvent {}
