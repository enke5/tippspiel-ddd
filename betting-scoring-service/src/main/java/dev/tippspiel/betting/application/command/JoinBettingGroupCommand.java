package dev.tippspiel.betting.application.command;

import java.util.UUID;

public record JoinBettingGroupCommand(
    UUID groupId,
    UUID playerId,
    String displayName
) {}
