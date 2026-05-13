package dev.tippspiel.betting.application.command;

import dev.tippspiel.betting.domain.model.*;
import dev.tippspiel.betting.domain.repository.BettingGroupRepository;
import dev.tippspiel.betting.domain.repository.MatchPredictionRepository;
import dev.tippspiel.betting.domain.repository.ParticipationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class BettingCommandService {

    private final BettingGroupRepository    groupRepository;
    private final ParticipationRepository   participationRepository;
    private final MatchPredictionRepository predictionRepository;

    public BettingCommandService(BettingGroupRepository groupRepository,
                                 ParticipationRepository participationRepository,
                                 MatchPredictionRepository predictionRepository) {
        this.groupRepository         = groupRepository;
        this.participationRepository = participationRepository;
        this.predictionRepository    = predictionRepository;
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
    public void joinBettingGroup(JoinBettingGroupCommand cmd) {
        BettingGroup group = groupRepository.findById(cmd.groupId())
                .orElseThrow(() -> new IllegalArgumentException("BettingGroup not found: " + cmd.groupId()));
        if (!group.isOpen()) {
            throw new IllegalStateException("BettingGroup is closed: " + cmd.groupId());
        }
        // Idempotent: do nothing if player already joined
        if (participationRepository.findByGroupAndPlayer(cmd.groupId(), cmd.playerId()).isPresent()) {
            return;
        }
        Participation participation = Participation.create(
                UUID.randomUUID(), cmd.groupId(), cmd.playerId(), cmd.displayName());
        participationRepository.save(participation);
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
