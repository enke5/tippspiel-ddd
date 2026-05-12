package dev.tippspiel.betting.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Minimal JPA entity for group_bonus_prediction.
 * Only the fields needed for bulk lock operations are mapped here.
 * Full CRUD will be added when the GroupBonusPrediction domain model is implemented.
 */
@Entity
@Table(name = "group_bonus_prediction")
class GroupBonusPredictionJpaEntity {

    @Id
    UUID id;

    @Column(name = "player_id", nullable = false)
    UUID playerId;

    @Column(name = "tournament_group_id", nullable = false, length = 10)
    String tournamentGroupId;

    @Column(name = "betting_group_id", nullable = false)
    UUID bettingGroupId;

    @Column(name = "predicted_first_team", length = 10)
    String predictedFirstTeam;

    @Column(name = "predicted_second_team", length = 10)
    String predictedSecondTeam;

    @Column(nullable = false, length = 10)
    String status;

    @Column(name = "points_awarded", nullable = false)
    int pointsAwarded;

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
