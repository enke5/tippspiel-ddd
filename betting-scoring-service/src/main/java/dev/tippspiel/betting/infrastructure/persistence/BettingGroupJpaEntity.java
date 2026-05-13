package dev.tippspiel.betting.infrastructure.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "betting_group")
class BettingGroupJpaEntity {

    @Id
    UUID id;

    @Column(nullable = false, length = 100)
    String name;

    @Column(nullable = false, length = 50, unique = true)
    String slug;

    @Column(name = "tournament_ref", nullable = false, length = 50)
    String tournamentRef;

    @Column(name = "stake_amount", nullable = false, precision = 8, scale = 2)
    BigDecimal stakeAmount;

    @Column(name = "stake_currency", nullable = false, length = 3)
    String stakeCurrency;

    @Column(nullable = false, length = 10)
    String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    Instant createdAt;

    @PrePersist
    void onInsert() {
        if (createdAt == null) createdAt = Instant.now();
    }
}
