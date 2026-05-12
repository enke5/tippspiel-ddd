package dev.tippspiel.betting.domain.service;

import dev.tippspiel.betting.domain.model.Participation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Calculates prize distribution for a completed BettingGroup.
 *
 * Algorithm (from DDD planning session 07):
 *
 *   totalPot = N × stakeAmount
 *
 *   Winners:
 *     minimumWinnerCount = max(1, floor(N / 10))
 *     When N < 10 → single prize tier, split equally among all tied 1st-place players.
 *     Otherwise   → power-tower tiers:
 *                     prize[k] = prize[k-1] ^ ζ    (k = 2, 3, …)
 *                     prize[1] = 10.00 (lowest tier, last-place winner)
 *                     ζ calibrated via bisection so that
 *                     sum of all prizes == totalPot  (tolerance ≤ 0.01 €)
 *
 *   Ties: players on the same rank all receive the full prize for that rank
 *         (the pot is split by rank, not by player count within a rank).
 *
 *   Result: each Participation's prizeAmount is set; zero for non-winners.
 */
public class PrizeDistributionService {

    private static final BigDecimal LOWEST_TIER         = new BigDecimal("10.00");
    private static final BigDecimal TOLERANCE           = new BigDecimal("0.01");
    private static final int        MAX_BISECT_ITERS    = 60;

    /**
     * Compute and assign prize amounts to all participations in the group.
     *
     * @param participations all participations in the betting group (any order)
     * @param stakeAmount    stake per player (e.g. 10 EUR)
     */
    public void distribute(List<Participation> participations, BigDecimal stakeAmount) {
        if (participations == null || participations.isEmpty()) return;

        int n = participations.size();
        BigDecimal totalPot = stakeAmount.multiply(BigDecimal.valueOf(n));

        // Sort descending by points
        List<Participation> ranked = new ArrayList<>(participations);
        ranked.sort(Comparator.comparingInt(Participation::getTotalPoints).reversed());

        // Build rank groups (tie-aware)
        List<List<Participation>> rankGroups = buildRankGroups(ranked);

        int minimumWinnerCount = Math.max(1, n / 10);

        // Collect winner groups until we cover at least minimumWinnerCount players
        List<List<Participation>> winnerGroups = selectWinnerGroups(rankGroups, minimumWinnerCount);
        int tierCount = winnerGroups.size();

        // Compute prize per tier
        List<BigDecimal> prizes = computePrizes(tierCount, totalPot, n);

        // Assign: best rank (index 0) gets highest prize (last element), worst gets prizes[0] = LOWEST_TIER
        // winnerGroups[0] = best rank → prizes[tierCount-1]
        for (int i = 0; i < tierCount; i++) {
            BigDecimal prize = prizes.get(tierCount - 1 - i);
            for (Participation p : winnerGroups.get(i)) {
                p.awardPrize(prize);
            }
        }
        // Non-winners get zero
        for (int i = tierCount; i < rankGroups.size(); i++) {
            for (Participation p : rankGroups.get(i)) {
                p.awardPrize(BigDecimal.ZERO);
            }
        }
    }

    // ─── Prize computation ────────────────────────────────────────────────────

    /**
     * Returns a list of prizes ordered ascending (prizes[0] = lowest tier = LOWEST_TIER,
     * prizes[tierCount-1] = top prize).
     *
     * For tierCount == 1: single prize = totalPot (split equally within tie handled upstream).
     * For tierCount >= 2: power tower with bisection-calibrated exponent ζ.
     */
    List<BigDecimal> computePrizes(int tierCount, BigDecimal totalPot, int playerCount) {
        if (tierCount == 1) {
            // Edge case: all winners tied — pot shared equally
            return List.of(totalPot);
        }

        // Power tower: prize[0] = LOWEST_TIER, prize[k] = prize[k-1]^ζ
        // Sum = LOWEST_TIER * (1 + ζ + ζ^2 + … + ζ^(tierCount-1)) but this is not geometric —
        // it's prize[k] = LOWEST_TIER ^ (ζ^k), so it grows super-exponentially.
        // We need sum(prize[0..tierCount-1]) == totalPot.
        //
        // prize[i] = BASE ^ (ζ^i), BASE = 10, i = 0..tierCount-1
        // Bisect on ζ in (1.0, upper bound).

        double pot      = totalPot.doubleValue();
        double base     = LOWEST_TIER.doubleValue();   // 10.0
        double tol      = TOLERANCE.doubleValue();

        double zeta = bisect(pot, base, tierCount, tol);

        // Build prize list
        List<BigDecimal> prizes = new ArrayList<>(tierCount);
        double current = base;
        for (int i = 0; i < tierCount; i++) {
            prizes.add(BigDecimal.valueOf(current).setScale(2, RoundingMode.HALF_UP));
            current = Math.pow(current, zeta);
        }
        return prizes;
    }

    /**
     * Bisection to find ζ such that sum of power-tower prizes ≈ totalPot.
     * prize[i] = base^(ζ^i) for i = 0..tierCount-1
     */
    private static double bisect(double totalPot, double base, int tierCount, double tol) {
        // Lower bound: ζ just above 1 → all prizes ≈ base (sum ≈ base * tierCount)
        double lo = 1.0 + 1e-9;
        // Upper bound: large ζ → top prize dominates. Use ζ = 10 as safe upper; expand if needed.
        double hi = 10.0;
        while (sumPrizes(base, hi, tierCount) < totalPot) {
            hi *= 2;
        }

        for (int iter = 0; iter < MAX_BISECT_ITERS; iter++) {
            double mid  = (lo + hi) / 2.0;
            double diff = sumPrizes(base, mid, tierCount) - totalPot;
            if (Math.abs(diff) <= tol) return mid;
            if (diff < 0) lo = mid;
            else          hi = mid;
        }
        return (lo + hi) / 2.0;
    }

    private static double sumPrizes(double base, double zeta, int tierCount) {
        double sum     = 0;
        double current = base;
        for (int i = 0; i < tierCount; i++) {
            sum    += current;
            current = Math.pow(current, zeta);
        }
        return sum;
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private static List<List<Participation>> buildRankGroups(List<Participation> sortedDesc) {
        List<List<Participation>> groups = new ArrayList<>();
        if (sortedDesc.isEmpty()) return groups;

        List<Participation> current = new ArrayList<>();
        current.add(sortedDesc.get(0));
        for (int i = 1; i < sortedDesc.size(); i++) {
            Participation p = sortedDesc.get(i);
            if (p.getTotalPoints() == current.get(0).getTotalPoints()) {
                current.add(p);
            } else {
                groups.add(current);
                current = new ArrayList<>();
                current.add(p);
            }
        }
        groups.add(current);
        return groups;
    }

    private static List<List<Participation>> selectWinnerGroups(
            List<List<Participation>> rankGroups, int minimumWinnerCount) {
        List<List<Participation>> winners = new ArrayList<>();
        int covered = 0;
        for (List<Participation> group : rankGroups) {
            winners.add(group);
            covered += group.size();
            if (covered >= minimumWinnerCount) break;
        }
        return winners;
    }
}
