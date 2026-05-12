package dev.tippspiel.tournament.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "match")
class MatchJpaEntity {

    @Id
    UUID id;

    @Column(nullable = false, length = 20)
    String round;

    @Column(name = "group_id", length = 10)
    String groupId;

    @Column(name = "home_placeholder", length = 100)
    String homePlaceholder;

    @Column(name = "away_placeholder", length = 100)
    String awayPlaceholder;

    @Column(name = "home_team_id", length = 10)
    String homeTeamId;

    @Column(name = "home_team_name", length = 100)
    String homeTeamName;

    @Column(name = "away_team_id", length = 10)
    String awayTeamId;

    @Column(name = "away_team_name", length = 100)
    String awayTeamName;

    @Column(name = "scheduled_for", nullable = false)
    Instant scheduledFor;

    @Column(nullable = false, length = 20)
    String status;

    @Column(name = "goals_home")
    Integer goalsHome;

    @Column(name = "goals_away")
    Integer goalsAway;

    @Column(length = 25)
    String phase;

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
