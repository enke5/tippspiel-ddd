package dev.tippspiel.tournament.application.command;

import dev.tippspiel.tournament.domain.model.MatchRound;

import java.time.Instant;
import java.util.UUID;

public record ScheduleMatchCommand(
    UUID matchId,
    MatchRound round,
    String groupId,           // null for knockout rounds
    String homePlaceholder,
    String awayPlaceholder,
    Instant scheduledFor
) {}
