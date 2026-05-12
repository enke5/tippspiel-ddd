package dev.tippspiel.betting.domain.repository;

import dev.tippspiel.betting.domain.model.MatchPrediction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MatchPredictionRepository {

    Optional<MatchPrediction> findByPlayerAndMatch(UUID playerId, UUID matchId, UUID bettingGroupId);

    List<MatchPrediction> findAllByMatch(UUID matchId);

    MatchPrediction save(MatchPrediction prediction);

    /** Lock all open predictions for a given match. Returns count of locked predictions. */
    int lockAllForMatch(UUID matchId);

    /** Lock all open group bonus predictions for a given tournament group. Returns count locked. */
    int lockGroupBonusPredictions(String groupId);
}
