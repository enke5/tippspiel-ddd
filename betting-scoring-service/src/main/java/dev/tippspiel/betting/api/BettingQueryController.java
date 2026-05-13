package dev.tippspiel.betting.api;

import dev.tippspiel.betting.query.BettingQueryService;
import dev.tippspiel.betting.query.LeaderboardEntry;
import dev.tippspiel.betting.query.PredictionStatusEntry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Read-side REST API (CQRS query side).
 *
 * GET /betting-groups/{id}/leaderboard
 * GET /betting-groups/{id}/players/{playerId}/predictions
 */
@RestController
@RequestMapping("/betting-groups")
public class BettingQueryController {

    private final BettingQueryService queryService;

    public BettingQueryController(BettingQueryService queryService) {
        this.queryService = queryService;
    }

    /**
     * Returns the leaderboard of a betting group sorted by rank ascending.
     * Ties share the same rank (dense-ish: 1,2,2,4,…).
     */
    @GetMapping("/{id}/leaderboard")
    public ResponseEntity<List<LeaderboardEntry>> getLeaderboard(@PathVariable UUID id) {
        List<LeaderboardEntry> board = queryService.getLeaderboard(id);
        if (board.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(board);
    }

    /**
     * Returns all submitted match predictions for a player within a betting group.
     */
    @GetMapping("/{id}/players/{playerId}/predictions")
    public ResponseEntity<List<PredictionStatusEntry>> getPredictions(
            @PathVariable UUID id,
            @PathVariable UUID playerId) {
        List<PredictionStatusEntry> predictions = queryService.getPredictionStatus(id, playerId);
        return ResponseEntity.ok(predictions);
    }
}
