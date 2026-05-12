package dev.tippspiel.betting.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface MatchPredictionJpaRepository extends JpaRepository<MatchPredictionJpaEntity, UUID> {

    Optional<MatchPredictionJpaEntity> findByPlayerIdAndMatchIdAndBettingGroupId(
            UUID playerId, UUID matchId, UUID bettingGroupId);

    List<MatchPredictionJpaEntity> findAllByMatchId(UUID matchId);

    @Modifying
    @Query("""
           UPDATE MatchPredictionJpaEntity p
           SET p.status = 'LOCKED'
           WHERE p.matchId = :matchId AND p.status = 'OPEN'
           """)
    int lockAllForMatch(@Param("matchId") UUID matchId);
}
