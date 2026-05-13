package dev.tippspiel.betting.infrastructure.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "participation")
class ParticipationJpaEntity {

    @Id
    UUID id;

    @Column(name = "betting_group_id", nullable = false)
    UUID bettingGroupId;

    @Column(name = "player_id", nullable = false)
    UUID playerId;

    @Column(name = "display_name", nullable = false, length = 100)
    String displayName;

    @Column(nullable = false)
    boolean approved;

    @Column(name = "stake_paid", nullable = false)
    boolean stakePaid;

    @Column(name = "total_points", nullable = false)
    int totalPoints;

    @Column(name = "prize_amount", precision = 8, scale = 2)
    BigDecimal prizeAmount;

    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt;

    @PrePersist
    void onInsert() {
        if (createdAt == null) createdAt = Instant.now();
    }
}
