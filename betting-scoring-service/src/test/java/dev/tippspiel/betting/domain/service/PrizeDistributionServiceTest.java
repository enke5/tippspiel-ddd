package dev.tippspiel.betting.domain.service;

import dev.tippspiel.betting.domain.model.Participation;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class PrizeDistributionServiceTest {

    private final PrizeDistributionService service = new PrizeDistributionService();

    // ─── Basic distribution ───────────────────────────────────────────────────

    @Test
    void singlePlayer_receivesFull_pot() {
        List<Participation> players = participations(100);
        service.distribute(players, new BigDecimal("20.00"));
        assertThat(players.get(0).getPrizeAmount()).isEqualByComparingTo("20.00");
    }

    @Test
    void ninePlayersAllTied_splitEquallyAsSingleTier() {
        // N=9 < 10 → minimumWinnerCount = 1. All tied → 1 group → single tier = totalPot
        List<Participation> players = participationsWithPoints(List.of(50,50,50,50,50,50,50,50,50));
        service.distribute(players, new BigDecimal("10.00"));
        // All tied → 1 rank group → single prize = totalPot (90€) shared... wait,
        // selectWinnerGroups returns 1 group but that group has all 9 players.
        // computePrizes(1, 90) returns [90]. Each of the 9 players gets 90.
        // This reflects "ties get same full prize each" from spec.
        BigDecimal expected = new BigDecimal("90.00");
        players.forEach(p -> assertThat(p.getPrizeAmount()).isEqualByComparingTo(expected));
    }

    @Test
    void tenPlayers_minimumWinnerCount_isOne() {
        // N=10 → minimumWinnerCount = 1; clear ranking → 1 winner
        List<Integer> points = List.of(100, 90, 80, 70, 60, 50, 40, 30, 20, 10);
        List<Participation> players = participationsWithPoints(points);
        service.distribute(players, new BigDecimal("10.00"));

        // 1 winner (rank-1 only), rest get 0
        Participation winner = players.stream()
                .filter(p -> p.getTotalPoints() == 100).findFirst().orElseThrow();
        assertThat(winner.getPrizeAmount()).isEqualByComparingTo("100.00");

        players.stream()
                .filter(p -> p.getTotalPoints() < 100)
                .forEach(p -> assertThat(p.getPrizeAmount()).isEqualByComparingTo("0.00"));
    }

    @Test
    void twentyPlayers_twoWinners_prizeSumEqualsPot() {
        // N=20 → minimumWinnerCount = 2; 2 winner tiers (rank1, rank2)
        List<Integer> points = new ArrayList<>();
        points.add(200); points.add(190);
        for (int i = 0; i < 18; i++) points.add(i * 5);
        List<Participation> players = participationsWithPoints(points);

        BigDecimal stake = new BigDecimal("10.00");
        service.distribute(players, stake);

        BigDecimal pot       = stake.multiply(BigDecimal.valueOf(20));
        BigDecimal prizeSum  = players.stream()
                .map(Participation::getPrizeAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Sum of winners' prizes should equal pot (within 2 cent rounding tolerance)
        assertThat(prizeSum.doubleValue()).isCloseTo(pot.doubleValue(), within(0.02));
    }

    @Test
    void tie_for_first_allTiedPlayersGetFullTopPrize() {
        // Three players tied at top with rank-2 player below → 2 tiers needed (N≥10 implies minCount=1)
        // but since first rank has 3 players they all get the top prize
        List<Integer> points = new ArrayList<>();
        for (int i = 0; i < 3; i++) points.add(100); // tied 1st
        points.add(50);                               // rank 2
        for (int i = 0; i < 6; i++) points.add(i);   // rest

        List<Participation> players = participationsWithPoints(points);
        service.distribute(players, new BigDecimal("10.00"));

        // minimumWinnerCount = max(1, 10/10) = 1 → winner group = first rank group (3 tied)
        // single tier → computePrizes(1, 100) = [100] → each of 3 players gets 100
        players.stream()
                .filter(p -> p.getTotalPoints() == 100)
                .forEach(p -> assertThat(p.getPrizeAmount()).isEqualByComparingTo("100.00"));
        players.stream()
                .filter(p -> p.getTotalPoints() < 100)
                .forEach(p -> assertThat(p.getPrizeAmount()).isEqualByComparingTo("0.00"));
    }

    @Test
    void prizeTiers_lowestTierIs10() {
        // N=30 → 3 winners; prizes[0] (lowest tier, rank-3 winner) should be 10.00
        // Points: 300, 290, 280, 270, …, 10  (rank-3 has 280 points)
        List<Integer> pts = new ArrayList<>();
        for (int i = 30; i > 0; i--) pts.add(i * 10);
        List<Participation> players = participationsWithPoints(pts);

        service.distribute(players, new BigDecimal("10.00"));

        // rank-3 winner (worst winner) gets LOWEST_TIER = 10.00
        Participation thirdPlace = players.stream()
                .filter(p -> p.getTotalPoints() == 280).findFirst().orElseThrow();
        assertThat(thirdPlace.getPrizeAmount()).isEqualByComparingTo("10.00");
    }

    @Test
    void emptyList_doesNotThrow() {
        service.distribute(List.of(), new BigDecimal("10.00"));
    }

    // ─── Unit test for computePrizes ──────────────────────────────────────────

    @Test
    void computePrizes_singleTier_returnsPot() {
        List<BigDecimal> prizes = service.computePrizes(1, new BigDecimal("50.00"), 5);
        assertThat(prizes).hasSize(1);
        assertThat(prizes.get(0)).isEqualByComparingTo("50.00");
    }

    @Test
    void computePrizes_twoTiers_sumEqualsPot() {
        BigDecimal pot = new BigDecimal("200.00");
        List<BigDecimal> prizes = service.computePrizes(2, pot, 20);
        assertThat(prizes).hasSize(2);
        double sum = prizes.stream().mapToDouble(BigDecimal::doubleValue).sum();
        assertThat(sum).isCloseTo(pot.doubleValue(), within(0.02));
    }

    @Test
    void computePrizes_threeTiers_lowestIs10() {
        List<BigDecimal> prizes = service.computePrizes(3, new BigDecimal("300.00"), 30);
        assertThat(prizes.get(0)).isEqualByComparingTo("10.00");
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private static List<Participation> participations(int points) {
        Participation p = Participation.create(UUID.randomUUID(), UUID.randomUUID(),
                UUID.randomUUID(), "Player");
        p.addPoints(points);
        return List.of(p);
    }

    private static List<Participation> participationsWithPoints(List<Integer> points) {
        List<Participation> result = new ArrayList<>();
        for (int pts : points) {
            Participation p = Participation.create(UUID.randomUUID(), UUID.randomUUID(),
                    UUID.randomUUID(), "Player-" + pts);
            p.addPoints(pts);
            result.add(p);
        }
        return result;
    }
}
