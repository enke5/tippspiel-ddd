package dev.tippspiel.tournament.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScoreTest {

    @Test
    void homeGoalsGreater_outcomeIsHomeWin() {
        assertThat(new Score(3, 1).outcome()).isEqualTo(Score.Outcome.HOME_WIN);
    }

    @Test
    void awayGoalsGreater_outcomeIsAwayWin() {
        assertThat(new Score(0, 1).outcome()).isEqualTo(Score.Outcome.AWAY_WIN);
    }

    @Test
    void equalGoals_outcomeIsDraw() {
        assertThat(new Score(2, 2).outcome()).isEqualTo(Score.Outcome.DRAW);
    }

    @Test
    void zeroZero_outcomeIsDraw() {
        assertThat(new Score(0, 0).outcome()).isEqualTo(Score.Outcome.DRAW);
    }

    @Test
    void negativeHomeGoals_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> new Score(-1, 0))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void negativeAwayGoals_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> new Score(2, -3))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
