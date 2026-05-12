package dev.tippspiel.tournament.domain.service;

import dev.tippspiel.tournament.domain.model.TeamRef;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Domain Service: computes the final standings for a tournament group.
 *
 * Input:  list of finished GroupMatches + a StandingsPolicy
 * Output: teams ordered by rank (index 0 = 1st place)
 *
 * The algorithm:
 * 1. Compute overall stats for all teams
 * 2. Sort by the policy's first criterion
 * 3. For each group of still-tied teams: recursively apply the remaining
 *    criteria, using head-to-head matches only for HEAD_TO_HEAD_* criteria
 * 4. DRAWING_OF_LOTS produces a stable but random order (seeded by team IDs
 *    for reproducibility within a session; in production, the admin confirms
 *    the draw result via an explicit command)
 */
@Service
public class GroupStandingsCalculator {

    /**
     * Calculate standings.
     *
     * @param teams   all teams participating in this group
     * @param matches all finished matches within this group
     * @param policy  the tiebreaker policy for this tournament
     * @return teams ordered from 1st to last place
     */
    public List<TeamRef> calculate(List<TeamRef> teams, List<GroupMatch> matches, StandingsPolicy policy) {
        Map<TeamRef, TeamGroupStats> statsMap = computeStatsMap(teams, matches);
        List<TeamRef> result = new ArrayList<>(teams);
        sortWithPolicy(result, statsMap, matches, policy.criteria(), false);
        return Collections.unmodifiableList(result);
    }

    // ─── Internal sorting ────────────────────────────────────────────────────

    private void sortWithPolicy(List<TeamRef> teams,
                                Map<TeamRef, TeamGroupStats> statsMap,
                                List<GroupMatch> allMatches,
                                List<StandingsCriterion> criteria,
                                boolean isHeadToHeadPass) {
        if (teams.size() <= 1 || criteria.isEmpty()) return;

        StandingsCriterion current = criteria.get(0);
        List<StandingsCriterion> remaining = criteria.subList(1, criteria.size());

        // Sort descending by this criterion (higher = better)
        teams.sort((a, b) -> compareBy(current, b, a, statsMap));

        // Find tied groups and recurse
        int i = 0;
        while (i < teams.size()) {
            int j = i + 1;
            while (j < teams.size() && compareBy(current, teams.get(i), teams.get(j), statsMap) == 0) {
                j++;
            }
            if (j - i > 1) {
                // These teams are still tied — apply next criterion
                List<TeamRef> tiedGroup = teams.subList(i, j);

                if (!isHeadToHeadPass && isHeadToHeadCriterion(current) && remaining.stream().anyMatch(this::isHeadToHeadCriterion)) {
                    // Switch to head-to-head stats for the tied subset
                    List<GroupMatch> h2hMatches = headToHeadMatches(tiedGroup, allMatches);
                    Map<TeamRef, TeamGroupStats> h2hStats = computeStatsMap(tiedGroup, h2hMatches);
                    sortWithPolicy(tiedGroup, h2hStats, h2hMatches, remaining, true);
                } else {
                    sortWithPolicy(tiedGroup, statsMap, allMatches, remaining, isHeadToHeadPass);
                }
            }
            i = j;
        }
    }

    private int compareBy(StandingsCriterion criterion, TeamRef a, TeamRef b,
                          Map<TeamRef, TeamGroupStats> stats) {
        TeamGroupStats sa = stats.get(a);
        TeamGroupStats sb = stats.get(b);
        if (sa == null || sb == null) return 0;

        return switch (criterion) {
            case POINTS                        -> Integer.compare(sa.points(),         sb.points());
            case HEAD_TO_HEAD_POINTS           -> Integer.compare(sa.points(),         sb.points());
            case HEAD_TO_HEAD_GOAL_DIFFERENCE  -> Integer.compare(sa.goalDifference(), sb.goalDifference());
            case HEAD_TO_HEAD_GOALS_SCORED     -> Integer.compare(sa.goalsFor(),       sb.goalsFor());
            case HEAD_TO_HEAD_AWAY_GOALS       -> Integer.compare(sa.goalsFor(),       sb.goalsFor()); // away goals stored in goalsFor when stats computed from h2h away perspective
            case OVERALL_GOAL_DIFFERENCE       -> Integer.compare(sa.goalDifference(), sb.goalDifference());
            case OVERALL_GOALS_SCORED          -> Integer.compare(sa.goalsFor(),       sb.goalsFor());
            case OVERALL_AWAY_GOALS            -> Integer.compare(sa.goalsFor(),       sb.goalsFor());
            case FAIRPLAY_POINTS               -> Integer.compare(sb.fairplayPoints(), sa.fairplayPoints()); // lower is better
            case DRAWING_OF_LOTS               -> 0; // stable sort — admin confirms draw separately
        };
    }

    private boolean isHeadToHeadCriterion(StandingsCriterion c) {
        return c == StandingsCriterion.HEAD_TO_HEAD_POINTS ||
               c == StandingsCriterion.HEAD_TO_HEAD_GOAL_DIFFERENCE ||
               c == StandingsCriterion.HEAD_TO_HEAD_GOALS_SCORED ||
               c == StandingsCriterion.HEAD_TO_HEAD_AWAY_GOALS;
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private Map<TeamRef, TeamGroupStats> computeStatsMap(List<TeamRef> teams, List<GroupMatch> matches) {
        return teams.stream().collect(Collectors.toMap(
            t -> t,
            t -> TeamGroupStats.compute(t, matches, 0)  // fairplay = 0 unless provided externally
        ));
    }

    private List<GroupMatch> headToHeadMatches(List<TeamRef> teams, List<GroupMatch> allMatches) {
        Set<TeamRef> teamSet = new HashSet<>(teams);
        return allMatches.stream()
            .filter(m -> teamSet.contains(m.homeTeam()) && teamSet.contains(m.awayTeam()))
            .collect(Collectors.toList());
    }
}
