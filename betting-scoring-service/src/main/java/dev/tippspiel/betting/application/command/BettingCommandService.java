package dev.tippspiel.betting.application.command;

import dev.tippspiel.betting.domain.model.*;
import dev.tippspiel.betting.domain.repository.MatchPredictionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class BettingCommandService {

    private final MatchPredictionRepository predictionRepository;

    public BettingCommandService(MatchPredictionRepository predictionRepository) {
        this.predictionRepository = predictionRepository;
    }

    @Transactional
    public UUID createBettingGroup(CreateBettingGroupCommand cmd) {
        UUID id = cmd.groupId() != null ? cmd.groupId() : UUID.randomUUID();
        // BettingGroup persistence will be added with BettingGroupRepository
        // For now, return generated ID so the controller has something to return
        return id;
    }

    @Transactional
    public void submitPrediction(SubmitPredictionCommand cmd) {
        MatchPrediction prediction = predictionRepository
                .findByPlayerAndMatch(cmd.playerId(), cmd.matchId(), cmd.bettingGroupId())
                .orElseGet(() -> MatchPrediction.create(
                        UUID.randomUUID(), cmd.playerId(), cmd.matchId(), cmd.bettingGroupId()));

        prediction.submit(new PredictedScore(cmd.goalsHome(), cmd.goalsAway()));
        predictionRepository.save(prediction);
    }
}
