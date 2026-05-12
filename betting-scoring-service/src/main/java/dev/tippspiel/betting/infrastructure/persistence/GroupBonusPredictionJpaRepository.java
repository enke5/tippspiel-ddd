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

    /** Both predicted positions exactly correct → 6 points. */
    @Modifying
    @Query("""
           UPDATE GroupBonusPredictionJpaEntity g
           SET g.pointsAwarded = 6, g.status = 'SCORED'
           WHERE g.tournamentGroupId = :groupId
             AND g.status = 'LOCKED'
             AND g.predictedFirstTeam  = :firstTeam
             AND g.predictedSecondTeam = :secondTeam
           """)
    int scoreBothCorrect(@Param("groupId")     String groupId,
                         @Param("firstTeam")   String firstTeam,
                         @Param("secondTeam")  String secondTeam);

    /** Exactly one predicted position correct → 2 points (skips already-scored rows). */
    @Modifying
    @Query("""
           UPDATE GroupBonusPredictionJpaEntity g
           SET g.pointsAwarded = 2, g.status = 'SCORED'
           WHERE g.tournamentGroupId = :groupId
             AND g.status = 'LOCKED'
             AND (g.predictedFirstTeam = :firstTeam OR g.predictedSecondTeam = :secondTeam)
             AND NOT (g.predictedFirstTeam = :firstTeam AND g.predictedSecondTeam = :secondTeam)
           """)
    int scoreOneCorrect(@Param("groupId")     String groupId,
                        @Param("firstTeam")   String firstTeam,
                        @Param("secondTeam")  String secondTeam);
}
