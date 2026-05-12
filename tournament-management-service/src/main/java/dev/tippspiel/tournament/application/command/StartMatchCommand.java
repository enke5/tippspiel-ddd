package dev.tippspiel.tournament.application.command;

import java.util.UUID;

public record StartMatchCommand(UUID matchId) {}
