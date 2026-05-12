package dev.tippspiel.betting.application.scoring;

import dev.tippspiel.betting.domain.model.PredictedScore;
import dev.tippspiel.events.tournament.EventTypes;
import org.junit.jupiter.api.Test;

import static dev.tippspiel.events.tournament.EventTypes.MatchRound.*;
import static org.assertj.core.api.Assertions.assertThat;

class MatchPredictionScoringServiceTest {

    // ─── Group stage ──────────────────────────────────────────────────────────

    @Test
    void groupStage_exactScore_gives3Points() {
        assertThat(score(predicted(2, 1), official(2, 1), GROUP_STAGE)).isEqualTo(3);
    }

    @Test
    void groupStage_correctOutcomeWin_gives1Point() {
        // predicted 3-0, official 1-0 — both HOME_WIN
        assertThat(score(predicted(3, 0), official(1, 0), GROUP_STAGE)).isEqualTo(1);
    }

    @Test
    void groupStage_correctOutcomeDraw_gives1Point() {
        assertThat(score(predicted(1, 1), official(0, 0), GROUP_STAGE)).isEqualTo(1);
    }

    @Test
    void groupStage_correctOutcomeAwayWin_gives1Point() {
        assertThat(score(predicted(0, 2), official(1, 3), GROUP_STAGE)).isEqualTo(1);
    }

    @Test
    void groupStage_wrongOutcome_gives0Points() {
        // predicted draw, actual home win
        assertThat(score(predicted(1, 1), official(2, 1), GROUP_STAGE)).isEqualTo(0);
    }

    @Test
    void groupStage_nullPrediction_gives0Points() {
        assertThat(score(null, official(1, 0), GROUP_STAGE)).isEqualTo(0);
    }

    // ─── Knockout rounds ──────────────────────────────────────────────────────

    @Test
    void knockout_exactScore_gives6Points() {
        assertThat(score(predicted(1, 0), official(1, 0), ROUND_OF_16)).isEqualTo(6);
    }

    @Test
    void knockout_correctOutcomeButNotExact_gives0Points() {
        assertThat(score(predicted(2, 0), official(1, 0), QUARTER_FINAL)).isEqualTo(0);
    }

    @Test
    void knockout_wrongResult_gives0Points() {
        assertThat(score(predicted(2, 1), official(0, 1), SEMI_FINAL)).isEqualTo(0);
    }

    @Test
    void knockout_final_exactScore_gives6Points() {
        assertThat(score(predicted(3, 2), official(3, 2), FINAL)).isEqualTo(6);
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private static int score(PredictedScore predicted, EventTypes.Score official, EventTypes.MatchRound round) {
        return MatchPredictionScoringService.computeMatchPoints(predicted, official, round);
    }

    private static PredictedScore predicted(int home, int away) {
        return new PredictedScore(home, away);
    }

    private static EventTypes.Score official(int home, int away) {
        return new EventTypes.Score(home, away);
    }
}
