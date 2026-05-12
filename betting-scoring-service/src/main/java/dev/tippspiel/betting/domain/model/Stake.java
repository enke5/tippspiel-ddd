package dev.tippspiel.betting.domain.model;

import java.math.BigDecimal;
import java.util.Currency;

/**
 * Value Object: the stake each participant pays to enter a BettingGroup.
 * Replaces the hardcoded 5€ in the legacy PHP implementation.
 */
public record Stake(BigDecimal amount, Currency currency) {

    public Stake {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Stake amount must be positive");
        }
        if (currency == null) {
            throw new IllegalArgumentException("Currency is required");
        }
    }

    public static Stake of(BigDecimal amount, String currencyCode) {
        return new Stake(amount, Currency.getInstance(currencyCode));
    }
}
