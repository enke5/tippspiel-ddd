package dev.tippspiel.tournament.infrastructure.persistence;

import dev.tippspiel.tournament.domain.model.TeamRef;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tournament_group")
class TournamentGroupJpaEntity {

    @Id
    UUID id;

    @Column(nullable = false, length = 20)
    String label;

    @Column(name = "tournament_ref", nullable = false, length = 50)
    String tournamentRef;

    @Column(name = "standings_policy_name", nullable = false, length = 100)
    String standingsPolicyName;

    @Convert(converter = TeamRefListConverter.class)
    @Column(nullable = false, columnDefinition = "jsonb")
    List<TeamRef> teams;

    @Column(nullable = false, length = 30)
    String status;

    @Convert(converter = TeamRefListConverter.class)
    @Column(name = "final_standings", columnDefinition = "jsonb")
    List<TeamRef> finalStandings;

    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    Instant updatedAt;

    @PrePersist
    void onInsert() {
        Instant now = Instant.now();
        if (createdAt == null) createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
