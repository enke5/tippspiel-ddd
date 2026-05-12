package dev.tippspiel.betting.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "match_prediction")
class MatchPredictionJpaEntity {

    @Id
    UUID id;

    @Column(name = "player_id", nullable = false)
    UUID playerId;

    @Column(name = "match_id", nullable = false)
    UUID matchId;

    @Column(name = "betting_group_id", nullable = false)
    UUID bettingGroupId;

    @Column(name = "group_id", length = 10)
    String groupId;

    @Column(name = "predicted_goals_home")
    Integer predictedGoalsHome;

    @Column(name = "predicted_goals_away")
    Integer predictedGoalsAway;

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
