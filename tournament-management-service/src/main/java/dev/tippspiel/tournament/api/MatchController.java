package dev.tippspiel.tournament.api;

import dev.tippspiel.tournament.application.command.*;
import dev.tippspiel.tournament.domain.model.MatchPhase;
import dev.tippspiel.tournament.domain.model.MatchRound;
import dev.tippspiel.tournament.domain.model.Side;
import dev.tippspiel.tournament.domain.model.TeamRef;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

/**
 * Admin REST API for match lifecycle management.
 *
 * POST /matches                     → schedule a match
 * POST /matches/{id}/fixture        → resolve a team placeholder (knockout only)
 * POST /matches/{id}/start          → start a match (closes tipping window)
 * POST /matches/{id}/finish         → record official result
 */
@RestController
@RequestMapping("/matches")
@PreAuthorize("hasRole('ADMIN')")
public class MatchController {

    private final MatchCommandService commandService;

    public MatchController(MatchCommandService commandService) {
        this.commandService = commandService;
    }

    // ─── Schedule ─────────────────────────────────────────────────────────────

    record ScheduleMatchRequest(
        UUID        matchId,          // optional — server generates if null
        String      round,            // MatchRound name, e.g. "GROUP_STAGE"
        String      groupId,          // null for knockout rounds
        String      homePlaceholder,
        String      awayPlaceholder,
        Instant     scheduledFor
    ) {}

    @PostMapping
    public ResponseEntity<Void> schedule(@RequestBody ScheduleMatchRequest req) {
        UUID id = commandService.scheduleMatch(new ScheduleMatchCommand(
                req.matchId() != null ? req.matchId() : UUID.randomUUID(),
                MatchRound.valueOf(req.round()),
                req.groupId(),
                req.homePlaceholder(),
                req.awayPlaceholder(),
                req.scheduledFor()
        ));

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(id).toUri();
        return ResponseEntity.created(location).build();
    }

    // ─── Resolve fixture ──────────────────────────────────────────────────────

    record ResolveFixtureRequest(
        String  placeholder,
        String  teamId,
        String  teamName,
        String  side            // "HOME" or "AWAY"
    ) {}

    @PostMapping("/{id}/fixture")
    public ResponseEntity<Void> resolveFixture(@PathVariable UUID id,
                                               @RequestBody ResolveFixtureRequest req) {
        commandService.resolveFixture(new ResolveMatchFixtureCommand(
                id,
                req.placeholder(),
                new TeamRef(req.teamId(), req.teamName()),
                Side.valueOf(req.side())
        ));
        return ResponseEntity.noContent().build();
    }

    // ─── Start ────────────────────────────────────────────────────────────────

    @PostMapping("/{id}/start")
    public ResponseEntity<Void> start(@PathVariable UUID id) {
        commandService.startMatch(new StartMatchCommand(id));
        return ResponseEntity.noContent().build();
    }

    // ─── Finish ───────────────────────────────────────────────────────────────

    record FinishMatchRequest(
        int     goalsHome,
        int     goalsAway,
        String  phase,          // MatchPhase name, e.g. "REGULAR_TIME"
        String  tournamentRef   // e.g. "UEFA_EURO_2024" — required for FINAL settlement
    ) {}

    @PostMapping("/{id}/finish")
    public ResponseEntity<Void> finish(@PathVariable UUID id,
                                       @RequestBody FinishMatchRequest req) {
        commandService.finishMatch(new FinishMatchCommand(
                id,
                req.goalsHome(),
                req.goalsAway(),
                MatchPhase.valueOf(req.phase()),
                req.tournamentRef()
        ));
        return ResponseEntity.noContent().build();
    }
}
