package dev.tippspiel.betting.application.command;

import java.util.UUID;

public record SubmitPredictionCommand(
    UUID playerId,
    UUID matchId,
    UUID bettingGroupId,
    int  goalsHome,
    int  goalsAway
) {}
