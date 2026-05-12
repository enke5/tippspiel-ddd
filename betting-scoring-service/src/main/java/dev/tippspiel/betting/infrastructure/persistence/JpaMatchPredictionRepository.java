package dev.tippspiel.betting.infrastructure.persistence;

import dev.tippspiel.betting.domain.model.MatchPrediction;
import dev.tippspiel.betting.domain.model.PredictedScore;
import dev.tippspiel.betting.domain.repository.MatchPredictionRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaMatchPredictionRepository implements MatchPredictionRepository {

    private final MatchPredictionJpaRepository predictionRepo;
    private final GroupBonusPredictionJpaRepository bonusRepo;

    public JpaMatchPredictionRepository(MatchPredictionJpaRepository predictionRepo,
                                        GroupBonusPredictionJpaRepository bonusRepo) {
        this.predictionRepo = predictionRepo;
        this.bonusRepo      = bonusRepo;
    }

    @Override
    public Optional<MatchPrediction> findByPlayerAndMatch(UUID playerId, UUID matchId, UUID bettingGroupId) {
        return predictionRepo
                .findByPlayerIdAndMatchIdAndBettingGroupId(playerId, matchId, bettingGroupId)
                .map(this::toDomain);
    }

    @Override
    public List<MatchPrediction> findAllByMatch(UUID matchId) {
        return predictionRepo.findAllByMatchId(matchId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public MatchPrediction save(MatchPrediction prediction) {
        MatchPredictionJpaEntity saved = predictionRepo.save(toEntity(prediction));
        return toDomain(saved);
    }

    @Override
    @Transactional
    public int lockAllForMatch(UUID matchId) {
        return predictionRepo.lockAllForMatch(matchId);
    }

    @Override
    @Transactional
    public int lockGroupBonusPredictions(String groupId) {
        return bonusRepo.lockAllForGroup(groupId);
    }

    @Override
    @Transactional
    public void scoreGroupBonusPredictions(String tournamentGroupId,
                                           String firstTeamId,
                                           String secondTeamId) {
        bonusRepo.scoreBothCorrect(tournamentGroupId, firstTeamId, secondTeamId);
        bonusRepo.scoreOneCorrect(tournamentGroupId, firstTeamId, secondTeamId);
    }

    // ─── Mapping ──────────────────────────────────────────────────────────────

    private MatchPrediction toDomain(MatchPredictionJpaEntity e) {
        PredictedScore predicted = (e.predictedGoalsHome != null && e.predictedGoalsAway != null)
                ? new PredictedScore(e.predictedGoalsHome, e.predictedGoalsAway)
                : null;

        return MatchPrediction.reconstitute(
                e.id,
                e.playerId,
                e.matchId,
                e.bettingGroupId,
                predicted,
                MatchPrediction.Status.valueOf(e.status),
                e.pointsAwarded
        );
    }

    private MatchPredictionJpaEntity toEntity(MatchPrediction p) {
        MatchPredictionJpaEntity e = new MatchPredictionJpaEntity();
        e.id             = p.getId();
        e.playerId       = p.getPlayerId();
        e.matchId        = p.getMatchId();
        e.bettingGroupId = p.getBettingGroupId();
        e.status         = p.getStatus().name();
        e.pointsAwarded  = p.getPointsAwarded();
        if (p.getPredictedScore() != null) {
            e.predictedGoalsHome = p.getPredictedScore().goalsHome();
            e.predictedGoalsAway = p.getPredictedScore().goalsAway();
        }
        return e;
    }
}
