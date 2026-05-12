package dev.tippspiel.betting.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

interface GroupBonusPredictionJpaRepository extends JpaRepository<GroupBonusPredictionJpaEntity, UUID> {

    @Modifying
    @Query("""
           UPDATE GroupBonusPredictionJpaEntity g
           SET g.status = 'LOCKED'
           WHERE g.tournamentGroupId = :groupId AND g.status = 'OPEN'
           """)
    int lockAllForGroup(@Param("groupId") String groupId);
}
