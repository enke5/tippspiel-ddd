package dev.tippspiel.betting.application.command;

import dev.tippspiel.betting.domain.model.*;
import dev.tippspiel.betting.domain.repository.BettingGroupRepository;
import dev.tippspiel.betting.domain.repository.MatchPredictionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class BettingCommandService {

    private final BettingGroupRepository      groupRepository;
    private final MatchPredictionRepository   predictionRepository;

    public BettingCommandService(BettingGroupRepository groupRepository,
                                 MatchPredictionRepository predictionRepository) {
        this.groupRepository      = groupRepository;
        this.predictionRepository = predictionRepository;
    }

    @Transactional
    public UUID createBettingGroup(CreateBettingGroupCommand cmd) {
        UUID id = cmd.groupId() != null ? cmd.groupId() : UUID.randomUUID();
        BettingGroup group = BettingGroup.create(
                id,
                cmd.name(),
                Stake.of(cmd.stakeAmount(), cmd.stakeCurrency()),
                cmd.tournamentRef()
        );
        groupRepository.save(group);
        return group.getId();
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
