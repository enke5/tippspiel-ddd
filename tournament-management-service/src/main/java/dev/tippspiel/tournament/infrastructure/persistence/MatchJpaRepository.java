package dev.tippspiel.tournament.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

interface MatchJpaRepository extends JpaRepository<MatchJpaEntity, UUID> {

    @Query("""
           SELECT COUNT(m) FROM MatchJpaEntity m
           WHERE m.groupId = :groupId
             AND m.id <> :excludeId
             AND m.status IN ('STARTED', 'FINISHED')
           """)
    long countStartedOrFinishedInGroup(
            @Param("groupId")   String groupId,
            @Param("excludeId") UUID   excludeId);
}
