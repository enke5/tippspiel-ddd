package dev.tippspiel.tournament.application.command;

import dev.tippspiel.tournament.domain.model.MatchPhase;

import java.util.UUID;

public record FinishMatchCommand(
    UUID matchId,
    int goalsHome,
    int goalsAway,
    MatchPhase phase
) {}
