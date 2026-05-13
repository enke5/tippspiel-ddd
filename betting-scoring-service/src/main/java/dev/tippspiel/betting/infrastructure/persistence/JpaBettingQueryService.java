package dev.tippspiel.betting.infrastructure.persistence;
import dev.tippspiel.betting.query.BettingQueryService;
import dev.tippspiel.betting.query.LeaderboardEntry;
import dev.tippspiel.betting.query.PredictionStatusEntry;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class JpaBettingQueryService implements BettingQueryService {

    private final EntityManager em;

    public JpaBettingQueryService(EntityManager em) {
        this.em = em;
    }

    // ─── Leaderboard ──────────────────────────────────────────────────────────

    @Override
    public List<LeaderboardEntry> getLeaderboard(UUID bettingGroupId) {
        TypedQuery<ParticipationJpaEntity> q = em.createQuery(
                """
                SELECT p FROM ParticipationJpaEntity p
                WHERE p.bettingGroupId = :gid
                ORDER BY p.totalPoints DESC, p.displayName ASC
                """,
                ParticipationJpaEntity.class);
        q.setParameter("gid", bettingGroupId);

        List<ParticipationJpaEntity> rows = q.getResultList();
        List<LeaderboardEntry> result = new ArrayList<>(rows.size());

        int rank = 1;
        for (int i = 0; i < rows.size(); i++) {
            ParticipationJpaEntity row = rows.get(i);
            // Assign same rank to ties
            if (i > 0 && rows.get(i - 1).totalPoints != row.totalPoints) {
                rank = i + 1;
            }
            result.add(new LeaderboardEntry(
                    rank,
                    row.playerId,
                    row.displayName,
                    row.totalPoints,
                    row.stakePaid,
                    row.approved,
                    row.prizeAmount
            ));
        }
        return result;
    }

    // ─── Prediction Status ────────────────────────────────────────────────────

    @Override
    public List<PredictionStatusEntry> getPredictionStatus(UUID bettingGroupId, UUID playerId) {
        TypedQuery<MatchPredictionJpaEntity> q = em.createQuery(
                """
                SELECT mp FROM MatchPredictionJpaEntity mp
                WHERE mp.bettingGroupId = :gid
                  AND mp.playerId       = :pid
                  AND mp.predictedGoalsHome IS NOT NULL
                ORDER BY mp.createdAt ASC
                """,
                MatchPredictionJpaEntity.class);
        q.setParameter("gid", bettingGroupId);
        q.setParameter("pid", playerId);

        List<PredictionStatusEntry> out = new ArrayList<>();
        for (MatchPredictionJpaEntity mp : q.getResultList()) {
            out.add(new PredictionStatusEntry(
                    mp.id,
                    mp.matchId,
                    mp.predictedGoalsHome,
                    mp.predictedGoalsAway,
                    mp.status,
                    mp.pointsAwarded
            ));
        }
        return out;
    }
}
