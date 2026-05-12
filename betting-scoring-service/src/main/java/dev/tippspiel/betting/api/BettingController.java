package dev.tippspiel.betting.api;

import dev.tippspiel.betting.application.command.BettingCommandService;
import dev.tippspiel.betting.application.command.CreateBettingGroupCommand;
import dev.tippspiel.betting.application.command.SubmitPredictionCommand;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.util.UUID;

/**
 * Player REST API for betting actions.
 *
 * POST /betting-groups                             → create a betting group
 * POST /betting-groups/{id}/predictions/match      → submit or update a match prediction
 */
@RestController
@RequestMapping("/betting-groups")
public class BettingController {

    private final BettingCommandService commandService;

    public BettingController(BettingCommandService commandService) {
        this.commandService = commandService;
    }

    // ─── Create betting group ─────────────────────────────────────────────────

    record CreateGroupRequest(
        String     name,
        BigDecimal stakeAmount,
        String     stakeCurrency,   // ISO 4217, e.g. "EUR"
        String     tournamentRef
    ) {}

    @PostMapping
    public ResponseEntity<Void> createGroup(@RequestBody CreateGroupRequest req) {
        UUID id = commandService.createBettingGroup(new CreateBettingGroupCommand(
                null,
                req.name(),
                req.stakeAmount(),
                req.stakeCurrency() != null ? req.stakeCurrency() : "EUR",
                req.tournamentRef()
        ));

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(location).build();
    }

    // ─── Submit match prediction ──────────────────────────────────────────────

    record SubmitPredictionRequest(
        UUID playerId,
        UUID matchId,
        int  goalsHome,
        int  goalsAway
    ) {}

    @PostMapping("/{groupId}/predictions/match")
    public ResponseEntity<Void> submitPrediction(@PathVariable UUID groupId,
                                                 @RequestBody SubmitPredictionRequest req) {
        commandService.submitPrediction(new SubmitPredictionCommand(
                req.playerId(),
                req.matchId(),
                groupId,
                req.goalsHome(),
                req.goalsAway()
        ));
        return ResponseEntity.noContent().build();
    }
}
