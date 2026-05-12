package dev.tippspiel.betting.application.eventhandler;

import dev.tippspiel.betting.domain.repository.MatchPredictionRepository;
import dev.tippspiel.events.tournament.MatchStarted;
import dev.tippspiel.events.tournament.TournamentManagementEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reacts to MatchStarted events from Tournament Management.
 *
 * Locks all MatchPredictions for the started match.
 * If isFirstMatchOfGroup, also locks GroupBonusPredictions for that group.
 */
@Component
public class TournamentEventHandler {

    private static final Logger log = LoggerFactory.getLogger(TournamentEventHandler.class);

    private final MatchPredictionRepository predictionRepository;

    public TournamentEventHandler(MatchPredictionRepository predictionRepository) {
        this.predictionRepository = predictionRepository;
    }

    @KafkaListener(
        topics = "tippspiel.tournament-management.events",
        groupId = "betting-scoring-service",
        containerFactory = "tournamentEventListenerFactory"
    )
    @Transactional
    public void handle(TournamentManagementEvent event) {
        switch (event) {
            case MatchStarted e    -> onMatchStarted(e);
            default                -> log.debug("Unhandled tournament event: {}", event.getClass().getSimpleName());
        }
    }

    private void onMatchStarted(MatchStarted event) {
        log.info("MatchStarted received: matchId={}, group={}, firstOfGroup={}",
            event.matchId(), event.groupId(), event.isFirstMatchOfGroup());

        int locked = predictionRepository.lockAllForMatch(event.matchId());
        log.info("Locked {} MatchPredictions for match {}", locked, event.matchId());

        if (event.isFirstMatchOfGroup() && event.groupId() != null) {
            int groupLocked = predictionRepository.lockGroupBonusPredictions(event.groupId());
            log.info("Locked {} GroupBonusPredictions for group {}", groupLocked, event.groupId());
        }
    }
}
