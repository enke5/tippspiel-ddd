package dev.tippspiel.tournament.domain.service;

import java.util.List;

/**
 * Value Object: a named, ordered list of tiebreaker criteria.
 *
 * Loaded from JSON via StandingsPolicyRegistry.
 * Stored by name on TournamentGroup.
 *
 * Example (UEFA Euro):
 * <pre>
 * {
 *   "name": "UEFA_EURO",
 *   "criteria": [
 *     "POINTS",
 *     "HEAD_TO_HEAD_POINTS",
 *     "HEAD_TO_HEAD_GOAL_DIFFERENCE",
 *     "HEAD_TO_HEAD_GOALS_SCORED",
 *     "OVERALL_GOAL_DIFFERENCE",
 *     "OVERALL_GOALS_SCORED",
 *     "DRAWING_OF_LOTS"
 *   ]
 * }
 * </pre>
 */
public record StandingsPolicy(String name, List<StandingsCriterion> criteria) {

    public StandingsPolicy {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Policy name required");
        if (criteria == null || criteria.isEmpty()) throw new IllegalArgumentException("At least one criterion required");
        criteria = List.copyOf(criteria); // immutable
    }

    /** Convenience: does this policy include head-to-head comparison? */
    public boolean hasHeadToHead() {
        return criteria.stream().anyMatch(c ->
            c == StandingsCriterion.HEAD_TO_HEAD_POINTS ||
            c == StandingsCriterion.HEAD_TO_HEAD_GOAL_DIFFERENCE ||
            c == StandingsCriterion.HEAD_TO_HEAD_GOALS_SCORED ||
            c == StandingsCriterion.HEAD_TO_HEAD_AWAY_GOALS
        );
    }
}
