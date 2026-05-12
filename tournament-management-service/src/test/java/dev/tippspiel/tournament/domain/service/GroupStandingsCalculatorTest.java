package dev.tippspiel.tournament.domain.service;

import dev.tippspiel.tournament.domain.model.Score;
import dev.tippspiel.tournament.domain.model.TeamRef;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GroupStandingsCalculatorTest {

    private final GroupStandingsCalculator calculator = new GroupStandingsCalculator();

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private static TeamRef team(String name) {
        return new TeamRef(UUID.randomUUID().toString(), name);
    }

    private static GroupMatch match(TeamRef home, TeamRef away, int homeGoals, int awayGoals) {
        return new GroupMatch(home, away, new Score(homeGoals, awayGoals));
    }

    private static StandingsPolicy policyOf(StandingsCriterion... criteria) {
        return new StandingsPolicy("TEST", List.of(criteria));
    }

    // ─── Clear points ordering ────────────────────────────────────────────────

    @Test
    void clearPointsOrder_ranksTeamsByDescendingPoints() {
        TeamRef germany  = team("Germany");
        TeamRef france   = team("France");
        TeamRef portugal = team("Portugal");

        // Germany 6pts, France 3pts, Portugal 0pts
        List<GroupMatch> matches = List.of(
            match(germany, france,    2, 0),
            match(germany, portugal,  1, 0),
            match(france,  portugal,  1, 0)
        );

        List<TeamRef> result = calculator.calculate(
            List.of(germany, france, portugal),
            matches,
            policyOf(StandingsCriterion.POINTS)
        );

        assertThat(result).containsExactly(germany, france, portugal);
    }

    // ─── Overall goal difference tiebreaker ───────────────────────────────────

    @Test
    void tieOnPoints_resolvedByOverallGoalDifference() {
        TeamRef germany  = team("Germany");
        TeamRef france   = team("France");
        TeamRef portugal = team("Portugal");

        // Circular 3-way tie: each team beats one and loses to one (3 pts each)
        // Using asymmetric scorelines to produce different GDs:
        //   Germany 1-0 France, France 1-0 Portugal, Portugal 2-0 Germany
        // Germany:  3pts, gf=1, ga=2, gd=-1
        // France:   3pts, gf=1, ga=1, gd=0  (actually wait: see calc below)
        // Portugal: 3pts, gf=2, ga=1, gd=+1
        //
        // Germany: home 1-0 France → gf+1,ga+0; away 0-2 Portugal → gf+0,ga+2 → gf=1,ga=2,gd=-1
        // France:  away 0-1 Germany → gf+0,ga+1; home 1-0 Portugal → gf+1,ga+0 → gf=1,ga=1,gd=0
        // Portugal: home 2-0 Germany → gf+2,ga+0; away 0-1 France → gf+0,ga+1 → gf=2,ga=1,gd=+1
        List<GroupMatch> matches = List.of(
            match(germany,  france,    1, 0),
            match(france,   portugal,  1, 0),
            match(portugal, germany,   2, 0)
        );

        List<TeamRef> result = calculator.calculate(
            List.of(germany, france, portugal),
            matches,
            policyOf(StandingsCriterion.POINTS, StandingsCriterion.OVERALL_GOAL_DIFFERENCE)
        );

        assertThat(result).containsExactly(portugal, france, germany);
    }

    // ─── Head-to-head tiebreaker ──────────────────────────────────────────────

    /**
     * Germany and France share identical full stats (6 pts, gd+1, gf=2, ga=1).
     * Germany beat France in the direct match → H2H resolves it.
     *
     * Match setup (round-robin of 4):
     *   Germany 1-0 France  (H2H: Germany wins)
     *   Germany 1-0 Portugal
     *   France  1-0 Portugal
     *   Hungary 1-0 Germany  ← both Germany/France lose once to balance stats
     *   France  1-0 Hungary
     *   Portugal 2-0 Hungary
     *
     * Germany:  6pts, gf=2 (1G+1P), ga=1 (H), gd=+1
     * France:   6pts, gf=2 (1P+1H), ga=1 (G), gd=+1  ← identical to Germany
     * Portugal: 3pts, gf=2 (2H), ga=2 (G+F), gd=0
     * Hungary:  3pts, gf=1 (G), ga=3 (F+P), gd=-2
     */
    @Test
    void headToHeadTiebreak_germanyBeatingFranceDirectlyRanksHigher() {
        TeamRef germany  = team("Germany");
        TeamRef france   = team("France");
        TeamRef portugal = team("Portugal");
        TeamRef hungary  = team("Hungary");

        List<GroupMatch> matches = List.of(
            match(germany,  france,    1, 0),
            match(germany,  portugal,  1, 0),
            match(france,   portugal,  1, 0),
            match(hungary,  germany,   1, 0),
            match(france,   hungary,   1, 0),
            match(portugal, hungary,   2, 0)
        );

        List<TeamRef> result = calculator.calculate(
            List.of(germany, france, portugal, hungary),
            matches,
            policyOf(
                StandingsCriterion.POINTS,
                StandingsCriterion.HEAD_TO_HEAD_POINTS,
                StandingsCriterion.HEAD_TO_HEAD_GOAL_DIFFERENCE,
                StandingsCriterion.OVERALL_GOAL_DIFFERENCE
            )
        );

        assertThat(result.get(0)).isEqualTo(germany);
        assertThat(result.get(1)).isEqualTo(france);
        assertThat(result.get(2)).isEqualTo(portugal);
        assertThat(result.get(3)).isEqualTo(hungary);
    }

    // ─── Overall goals scored as second tiebreaker ────────────────────────────

    @Test
    void tieOnPointsAndGoalDifference_resolvedByOverallGoalsScored() {
        TeamRef a = team("A");
        TeamRef b = team("B");
        TeamRef c = team("C");

        // A and B both 3pts and gd=0, but A scored more overall goals
        // A 2-1 C, B 1-0 C, A 0-0 B
        // A: 3pts (beat C) + 1pt (drew B) = 4pts — not equal!

        // Simpler: only 2 teams in a mini 2-match tournament
        // A draws B 2-2, A beats C 1-0   → 4pts, gf=3, ga=2, gd=+1
        // B draws A 2-2, B beats C 2-0   → 4pts, gf=4, ga=2, gd=+2 — different GD again

        // Use: A 1-0 C, B 1-0 C, A 0-0 B
        // A: 1pt (drew B) + 3pts (beat C) = 4pts, gf=1, ga=0, gd=+1
        // B: 1pt (drew A) + 3pts (beat C) = 4pts, gf=1, ga=0, gd=+1 ← same!
        // Both 4pts, gd=+1, gf=1... still equal!

        // A 2-0 C, B 1-0 C, A 0-0 B
        // A: drew B (1pt), beat C 2-0 (3pts) = 4pts, gf=2, ga=0, gd=+2
        // B: drew A (1pt), beat C 1-0 (3pts) = 4pts, gf=1, ga=0, gd=+1
        // Different GD — won't reach GOALS_SCORED criterion.

        // Best approach: equal GD but different goals scored
        // A 3-2 B, B 3-2 C, A 0-1 C
        // A: beat B 3-2 (3pts), lost to C 0-1 (0pts) = 3pts, gf=3, ga=3, gd=0
        // B: lost to A 2-3 (0pts), beat C 3-2 (3pts) = 3pts, gf=5, ga=5, gd=0
        // C: beat A 1-0 (3pts), lost to B 2-3 (0pts) = 3pts, gf=3, ga=3, gd=0
        // A and C: same stats. B: 5 goals scored.
        // With POINTS → GD → GOALS_SCORED: B first (5 gf), then A and C equal (3 gf each)
        List<GroupMatch> matches = List.of(
            match(a, b,  3, 2),
            match(b, c,  3, 2),
            match(c, a,  1, 0)
        );

        List<TeamRef> result = calculator.calculate(
            List.of(a, b, c),
            matches,
            policyOf(
                StandingsCriterion.POINTS,
                StandingsCriterion.OVERALL_GOAL_DIFFERENCE,
                StandingsCriterion.OVERALL_GOALS_SCORED,
                StandingsCriterion.DRAWING_OF_LOTS
            )
        );

        // B first (3pts, gd=0, 5 goals scored); A and C tied → stable order → A before C (input order)
        assertThat(result.get(0)).isEqualTo(b);
        assertThat(result).containsExactlyInAnyOrder(a, b, c);
    }

    // ─── DRAWING_OF_LOTS preserves stable input order ─────────────────────────

    @Test
    void drawingOfLots_allStatsEqual_preservesInputOrder() {
        TeamRef alpha = team("Alpha");
        TeamRef beta  = team("Beta");
        TeamRef gamma = team("Gamma");

        // All teams draw all matches → equal in every stat
        List<GroupMatch> matches = List.of(
            match(alpha, beta,  1, 1),
            match(alpha, gamma, 1, 1),
            match(beta,  gamma, 1, 1)
        );
        // All: 2pts, gf=2, ga=2, gd=0

        List<TeamRef> result = calculator.calculate(
            List.of(alpha, beta, gamma),
            matches,
            policyOf(
                StandingsCriterion.POINTS,
                StandingsCriterion.OVERALL_GOAL_DIFFERENCE,
                StandingsCriterion.DRAWING_OF_LOTS
            )
        );

        // DRAWING_OF_LOTS → compareBy returns 0 → Java's stable sort preserves input order
        assertThat(result).containsExactly(alpha, beta, gamma);
    }

    // ─── Edge cases ───────────────────────────────────────────────────────────

    @Test
    void singleTeam_returnsItUnchanged() {
        TeamRef only = team("OnlyTeam");

        List<TeamRef> result = calculator.calculate(
            List.of(only),
            List.of(),
            policyOf(StandingsCriterion.POINTS)
        );

        assertThat(result).containsExactly(only);
    }

    @Test
    void noMatchesPlayed_allTeamsEqualOnZeroPoints_preservesInputOrder() {
        TeamRef a = team("A");
        TeamRef b = team("B");

        List<TeamRef> result = calculator.calculate(
            List.of(a, b),
            List.of(),
            policyOf(StandingsCriterion.POINTS, StandingsCriterion.DRAWING_OF_LOTS)
        );

        assertThat(result).containsExactly(a, b);
    }
}
