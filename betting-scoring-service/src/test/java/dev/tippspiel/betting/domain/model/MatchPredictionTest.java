package dev.tippspiel.betting.domain.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MatchPredictionTest {

    private static MatchPrediction freshPrediction() {
        return MatchPrediction.create(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());
    }

    // ─── Initial state ────────────────────────────────────────────────────────

    @Test
    void create_hasOpenStatusAndZeroPoints() {
        MatchPrediction prediction = freshPrediction();

        assertThat(prediction.getStatus()).isEqualTo(MatchPrediction.Status.OPEN);
        assertThat(prediction.getPointsAwarded()).isZero();
        assertThat(prediction.getPredictedScore()).isNull();
        assertThat(prediction.isOpen()).isTrue();
    }

    // ─── submit() ─────────────────────────────────────────────────────────────

    @Test
    void submit_whileOpen_setsPredictedScore() {
        MatchPrediction prediction = freshPrediction();
        PredictedScore score = new PredictedScore(2, 1);

        prediction.submit(score);

        assertThat(prediction.getPredictedScore()).isEqualTo(score);
        assertThat(prediction.getStatus()).isEqualTo(MatchPrediction.Status.OPEN);
    }

    @Test
    void submit_canBeUpdatedWhileOpen() {
        MatchPrediction prediction = freshPrediction();
        prediction.submit(new PredictedScore(1, 0));
        prediction.submit(new PredictedScore(3, 2));

        assertThat(prediction.getPredictedScore()).isEqualTo(new PredictedScore(3, 2));
    }

    @Test
    void submit_afterLock_throwsIllegalStateException() {
        MatchPrediction prediction = freshPrediction();
        prediction.lock();

        assertThatThrownBy(() -> prediction.submit(new PredictedScore(0, 0)))
            .isInstanceOf(IllegalStateException.class);
    }

    // ─── lock() ───────────────────────────────────────────────────────────────

    @Test
    void lock_fromOpen_transitionsToLocked() {
        MatchPrediction prediction = freshPrediction();
        prediction.lock();

        assertThat(prediction.getStatus()).isEqualTo(MatchPrediction.Status.LOCKED);
    }

    @Test
    void lock_whenAlreadyLocked_isIdempotent() {
        MatchPrediction prediction = freshPrediction();
        prediction.lock();
        prediction.lock(); // must not throw

        assertThat(prediction.getStatus()).isEqualTo(MatchPrediction.Status.LOCKED);
    }

    @Test
    void lock_whenScored_doesNotRevertToLocked() {
        MatchPrediction prediction = freshPrediction();
        prediction.lock();
        prediction.score(3);

        prediction.lock(); // idempotent guard when SCORED

        assertThat(prediction.getStatus()).isEqualTo(MatchPrediction.Status.SCORED);
    }

    // ─── score() ──────────────────────────────────────────────────────────────

    @Test
    void score_fromLocked_setsPointsAndTransitionsToScored() {
        MatchPrediction prediction = freshPrediction();
        prediction.lock();
        prediction.score(3);

        assertThat(prediction.getStatus()).isEqualTo(MatchPrediction.Status.SCORED);
        assertThat(prediction.getPointsAwarded()).isEqualTo(3);
    }

    @Test
    void score_withZeroPoints_setsZeroAndTransitionsToScored() {
        MatchPrediction prediction = freshPrediction();
        prediction.lock();
        prediction.score(0);

        assertThat(prediction.getStatus()).isEqualTo(MatchPrediction.Status.SCORED);
        assertThat(prediction.getPointsAwarded()).isZero();
    }

    @Test
    void score_fromOpen_throwsIllegalStateException() {
        MatchPrediction prediction = freshPrediction();

        assertThatThrownBy(() -> prediction.score(3))
            .isInstanceOf(IllegalStateException.class);
    }
}
