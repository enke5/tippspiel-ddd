package dev.tippspiel.betting.application.eventhandler;

import dev.tippspiel.betting.application.scoring.MatchPredictionScoringService;
import dev.tippspiel.betting.domain.repository.MatchPredictionRepository;
import dev.tippspiel.events.tournament.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reacts to all relevant Tournament Management events:
 *
 *   MatchStarted           → lock predictions (close tipping window)
 *   MatchFinished          → score match predictions
 *   TournamentGroupCompleted → score group bonus predictions
 */
@Component
public class TournamentEventHandler {

    private static final Logger log = LoggerFactory.getLogger(TournamentEventHandler.class);

    private final MatchPredictionRepository     predictionRepository;
    private final MatchPredictionScoringService scoringService;

    public TournamentEventHandler(MatchPredictionRepository predictionRepository,
                                  MatchPredictionScoringService scoringService) {
        this.predictionRepository = predictionRepository;
        this.scoringService       = scoringService;
    }

    @KafkaListener(
        topics = "tippspiel.tournament-management.events",
        groupId = "betting-scoring-service",
        containerFactory = "tournamentEventListenerFactory"
    )
    @Transactional
    public void handle(TournamentManagementEvent event) {
        switch (event) {
            case MatchStarted           e -> onMatchStarted(e);
            case MatchFinished          e -> onMatchFinished(e);
            case TournamentGroupCompleted e -> onGroupCompleted(e);
            default -> log.debug("Unhandled tournament event: {}", event.getClass().getSimpleName());
        }
    }

    // ─── Handlers ─────────────────────────────────────────────────────────────

    private void onMatchStarted(MatchStarted event) {
        log.info("MatchStarted: matchId={}, group={}, firstOfGroup={}",
                event.matchId(), event.groupId(), event.isFirstMatchOfGroup());

        int locked = predictionRepository.lockAllForMatch(event.matchId());
        log.info("Locked {} MatchPredictions for match {}", locked, event.matchId());

        if (event.isFirstMatchOfGroup() && event.groupId() != null) {
            int groupLocked = predictionRepository.lockGroupBonusPredictions(event.groupId());
            log.info("Locked {} GroupBonusPredictions for group {}", groupLocked, event.groupId());
        }
    }

    private void onMatchFinished(MatchFinished event) {
        log.info("MatchFinished: matchId={}, round={}, score={}:{}",
                event.matchId(), event.round(),
                event.score().goalsHome(), event.score().goalsAway());

        scoringService.scoreMatchPredictions(event.matchId(), event.score(), event.round());
    }

    private void onGroupCompleted(TournamentGroupCompleted event) {
        var standings = event.finalStandings();
        if (standings == null || standings.size() < 2) {
            log.warn("TournamentGroupCompleted for {} has insufficient standings — skipping bonus scoring",
                    event.group().groupId());
            return;
        }
        String firstTeamId  = standings.get(0).team().teamId();
        String secondTeamId = standings.get(1).team().teamId();

        log.info("TournamentGroupCompleted: group={}, 1st={}, 2nd={}",
                event.group().groupId(), firstTeamId, secondTeamId);

        scoringService.scoreGroupBonusPredictions(event.group().groupId(), firstTeamId, secondTeamId);
    }
}

