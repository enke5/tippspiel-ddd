package dev.tippspiel.tournament.domain.service;

import dev.tippspiel.tournament.domain.model.TeamRef;

import java.util.List;

/**
 * Aggregated stats for a single team within a group, computed from a set of GroupMatches.
 *
 * Immutable value object. Used internally by GroupStandingsCalculator.
 */
public record TeamGroupStats(
    TeamRef team,
    int     matchesPlayed,
    int     wins,
    int     draws,
    int     losses,
    int     goalsFor,
    int     goalsAgainst,
    int     fairplayPoints   // 0 if card data not available
) {

    public int points()         { return wins * 3 + draws; }
    public int goalDifference() { return goalsFor - goalsAgainst; }

    /**
     * Compute stats for one team from a list of finished group matches.
     * Only matches in which the team participated are considered.
     */
    public static TeamGroupStats compute(TeamRef team, List<GroupMatch> matches, int fairplayPoints) {
        int played = 0, wins = 0, draws = 0, losses = 0, gf = 0, ga = 0;

        for (GroupMatch m : matches) {
            boolean isHome = m.homeTeam().equals(team);
            boolean isAway = m.awayTeam().equals(team);
            if (!isHome && !isAway) continue;

            played++;
            if (isHome) {
                gf += m.score().goalsHome();
                ga += m.score().goalsAway();
                wins   += m.score().outcome() == dev.tippspiel.tournament.domain.model.Score.Outcome.HOME_WIN ? 1 : 0;
                draws  += m.isDraw() ? 1 : 0;
                losses += m.score().outcome() == dev.tippspiel.tournament.domain.model.Score.Outcome.AWAY_WIN ? 1 : 0;
            } else {
                gf += m.score().goalsAway();
                ga += m.score().goalsHome();
                wins   += m.score().outcome() == dev.tippspiel.tournament.domain.model.Score.Outcome.AWAY_WIN ? 1 : 0;
                draws  += m.isDraw() ? 1 : 0;
                losses += m.score().outcome() == dev.tippspiel.tournament.domain.model.Score.Outcome.HOME_WIN ? 1 : 0;
            }
        }
        return new TeamGroupStats(team, played, wins, draws, losses, gf, ga, fairplayPoints);
    }
}
