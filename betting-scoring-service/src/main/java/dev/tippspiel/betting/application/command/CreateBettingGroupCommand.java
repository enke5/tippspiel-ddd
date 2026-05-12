package dev.tippspiel.betting.application.command;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateBettingGroupCommand(
    UUID       groupId,       // optional — server generates if null
    String     name,
    BigDecimal stakeAmount,
    String     stakeCurrency, // ISO 4217, e.g. "EUR"
    String     tournamentRef
) {}
