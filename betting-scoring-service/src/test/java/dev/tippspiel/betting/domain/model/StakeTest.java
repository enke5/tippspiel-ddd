package dev.tippspiel.betting.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StakeTest {

    @Test
    void validStake_createsSuccessfully() {
        Stake stake = Stake.of(new BigDecimal("5.00"), "EUR");

        assertThat(stake.amount()).isEqualByComparingTo("5.00");
        assertThat(stake.currency().getCurrencyCode()).isEqualTo("EUR");
    }

    @Test
    void zeroAmount_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> Stake.of(BigDecimal.ZERO, "EUR"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void negativeAmount_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> Stake.of(new BigDecimal("-1.00"), "EUR"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nullAmount_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> Stake.of(null, "EUR"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void invalidCurrencyCode_throwsException() {
        assertThatThrownBy(() -> Stake.of(BigDecimal.TEN, "INVALID"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void differentCurrencies_areNotEqual() {
        Stake eur = Stake.of(new BigDecimal("10"), "EUR");
        Stake usd = Stake.of(new BigDecimal("10"), "USD");

        assertThat(eur).isNotEqualTo(usd);
    }
}
