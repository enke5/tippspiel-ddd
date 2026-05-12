package dev.tippspiel.tournament.application.command;

import dev.tippspiel.tournament.domain.model.Side;
import dev.tippspiel.tournament.domain.model.TeamRef;

import java.util.UUID;

public record ResolveMatchFixtureCommand(
    UUID matchId,
    String placeholder,
    TeamRef team,
    Side side
) {}
