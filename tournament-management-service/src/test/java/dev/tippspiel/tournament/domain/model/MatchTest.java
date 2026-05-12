package dev.tippspiel.tournament.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MatchTest {

    private static Match scheduledMatch(MatchRound round, String groupId) {
        return Match.schedule(UUID.randomUUID(), round, groupId, "Home FC", "Away FC", Instant.now());
    }

    private static TeamRef team(String name) {
        return new TeamRef(UUID.randomUUID().toString(), name);
    }

    // ─── Initial state ────────────────────────────────────────────────────────

    @Test
    void schedule_createsMatchWithScheduledStatus() {
        Match match = scheduledMatch(MatchRound.GROUP_STAGE, "A");

        assertThat(match.getStatus()).isEqualTo(Match.Status.SCHEDULED);
        assertThat(match.getScore()).isNull();
        assertThat(match.getPhase()).isNull();
    }

    // ─── start() ─────────────────────────────────────────────────────────────

    @Test
    void start_fromScheduled_transitionsToStarted() {
        Match match = scheduledMatch(MatchRound.GROUP_STAGE, "A");
        match.start();

        assertThat(match.getStatus()).isEqualTo(Match.Status.STARTED);
    }

    @Test
    void start_fromFixtureResolved_transitionsToStarted() {
        Match match = scheduledMatch(MatchRound.ROUND_OF_16, null);
        match.resolveFixture("Home FC", team("Real Madrid"), Side.HOME);
        match.resolveFixture("Away FC", team("Bayern Munich"), Side.AWAY);
        match.start();

        assertThat(match.getStatus()).isEqualTo(Match.Status.STARTED);
    }

    @Test
    void start_fromFinished_throwsIllegalStateException() {
        Match match = scheduledMatch(MatchRound.GROUP_STAGE, "A");
        match.start();
        match.finish(new Score(2, 1), MatchPhase.REGULAR_TIME);

        assertThatThrownBy(match::start)
            .isInstanceOf(IllegalStateException.class);
    }

    // ─── finish() ────────────────────────────────────────────────────────────

    @Test
    void finish_fromStarted_setsScorePhaseAndFinishedStatus() {
        Match match = scheduledMatch(MatchRound.QUARTER_FINAL, null);
        match.start();
        match.finish(new Score(1, 1), MatchPhase.PENALTY_SHOOTOUT);

        assertThat(match.getStatus()).isEqualTo(Match.Status.FINISHED);
        assertThat(match.getScore()).isEqualTo(new Score(1, 1));
        assertThat(match.getPhase()).isEqualTo(MatchPhase.PENALTY_SHOOTOUT);
    }

    @Test
    void finish_fromScheduled_throwsIllegalStateException() {
        Match match = scheduledMatch(MatchRound.GROUP_STAGE, "B");

        assertThatThrownBy(() -> match.finish(new Score(0, 0), MatchPhase.REGULAR_TIME))
            .isInstanceOf(IllegalStateException.class);
    }

    // ─── resolveFixture() ────────────────────────────────────────────────────

    @Test
    void resolveFixture_bothSides_transitionsToFixtureResolved() {
        Match match = scheduledMatch(MatchRound.SEMI_FINAL, null);
        TeamRef home = team("Brazil");
        TeamRef away = team("Argentina");

        match.resolveFixture("Home FC", home, Side.HOME);
        assertThat(match.getStatus()).isEqualTo(Match.Status.SCHEDULED); // not yet resolved (only one side)

        match.resolveFixture("Away FC", away, Side.AWAY);
        assertThat(match.getStatus()).isEqualTo(Match.Status.FIXTURE_RESOLVED);
        assertThat(match.getHomeTeam()).isEqualTo(home);
        assertThat(match.getAwayTeam()).isEqualTo(away);
    }

    @Test
    void resolveFixture_afterMatchStarted_throwsIllegalStateException() {
        Match match = scheduledMatch(MatchRound.GROUP_STAGE, "C");
        match.start();

        assertThatThrownBy(() -> match.resolveFixture("Home FC", team("X"), Side.HOME))
            .isInstanceOf(IllegalStateException.class);
    }

    // ─── isGroupStageMatch() ──────────────────────────────────────────────────

    @Test
    void isGroupStageMatch_groupStageRound_returnsTrue() {
        Match match = scheduledMatch(MatchRound.GROUP_STAGE, "D");
        assertThat(match.isGroupStageMatch()).isTrue();
    }

    @Test
    void isGroupStageMatch_knockoutRound_returnsFalse() {
        Match match = scheduledMatch(MatchRound.ROUND_OF_16, null);
        assertThat(match.isGroupStageMatch()).isFalse();
    }
}
