package dev.tippspiel.betting.api;

import dev.tippspiel.betting.application.settlement.SettlementService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Internal admin API — not exposed to end-users.
 *
 * POST /admin/betting-groups/{id}/settle
 *   Manually trigger prize distribution for a betting group.
 *   Idempotent: already-closed groups are skipped (returns 204).
 */
@RestController
@RequestMapping("/admin/betting-groups")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final SettlementService settlementService;

    public AdminController(SettlementService settlementService) {
        this.settlementService = settlementService;
    }

    @PostMapping("/{id}/settle")
    public ResponseEntity<Void> settle(@PathVariable UUID id) {
        settlementService.settle(id);
        return ResponseEntity.noContent().build();
    }
}
