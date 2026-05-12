package dev.tippspiel.tournament.domain.service;

/**
 * A single tiebreaker criterion used to rank teams with equal points.
 *
 * Criteria are applied in the order defined by the StandingsPolicy.
 * All HEAD_TO_HEAD criteria are applied using only the matches played
 * between the tied teams — not overall group stats.
 */
public enum StandingsCriterion {

    /** Primary ranking: 3pts win, 1pt draw, 0pts loss. */
    POINTS,

    /** Head-to-head: points from matches between the tied teams only. */
    HEAD_TO_HEAD_POINTS,

    /** Head-to-head: goal difference in matches between the tied teams only. */
    HEAD_TO_HEAD_GOAL_DIFFERENCE,

    /** Head-to-head: goals scored in matches between the tied teams only. */
    HEAD_TO_HEAD_GOALS_SCORED,

    /** Head-to-head: away goals in matches between the tied teams (historic, some competitions). */
    HEAD_TO_HEAD_AWAY_GOALS,

    /** Overall goal difference across all group matches. */
    OVERALL_GOAL_DIFFERENCE,

    /** Overall goals scored across all group matches. */
    OVERALL_GOALS_SCORED,

    /** Overall away goals scored (used by some domestic cup formats). */
    OVERALL_AWAY_GOALS,

    /**
     * FIFA Fairplay: lowest cumulative card points.
     * (Yellow=1, Yellow+Yellow=3, Red=3, Yellow+Red=4)
     * Requires card data — if not available, this criterion is skipped.
     */
    FAIRPLAY_POINTS,

    /**
     * Drawing of lots / random assignment.
     * Used as the final tiebreaker when all other criteria are exhausted.
     */
    DRAWING_OF_LOTS
}
