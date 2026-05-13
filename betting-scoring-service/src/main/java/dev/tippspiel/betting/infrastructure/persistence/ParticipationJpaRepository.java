package dev.tippspiel.betting.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface ParticipationJpaRepository extends JpaRepository<ParticipationJpaEntity, UUID> {

    Optional<ParticipationJpaEntity> findByBettingGroupIdAndPlayerId(UUID bettingGroupId, UUID playerId);

    List<ParticipationJpaEntity> findAllByBettingGroupIdOrderByTotalPointsDesc(UUID bettingGroupId);
}
