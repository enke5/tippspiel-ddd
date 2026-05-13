package dev.tippspiel.betting.api;

import dev.tippspiel.betting.domain.model.BettingGroup;
import dev.tippspiel.betting.domain.repository.BettingGroupRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * Group lookup API — resolves a subdomain slug to a betting group.
 *
 * GET /groups?slug=arbeit  →  { "id": "...", "name": "...", "slug": "arbeit", "status": "OPEN" }
 */
@RestController
@RequestMapping("/groups")
public class GroupQueryController {

    private final BettingGroupRepository groupRepository;

    public GroupQueryController(BettingGroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    record GroupSummary(UUID id, String name, String slug, String status) {}

    @GetMapping
    public ResponseEntity<GroupSummary> findBySlug(@RequestParam String slug) {
        return groupRepository.findBySlug(slug)
                .map(g -> ResponseEntity.ok(new GroupSummary(g.getId(), g.getName(), g.getSlug(), g.getStatus().name())))
                .orElse(ResponseEntity.notFound().build());
    }
}
