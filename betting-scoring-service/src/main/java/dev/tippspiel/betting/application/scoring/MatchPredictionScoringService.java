package dev.tippspiel.betting.application.scoring;

import dev.tippspiel.betting.domain.model.MatchPrediction;
import dev.tippspiel.betting.domain.model.PredictedScore;
import dev.tippspiel.betting.domain.repository.MatchPredictionRepository;
import dev.tippspiel.betting.domain.repository.ParticipationRepository;
import dev.tippspiel.events.tournament.EventTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Application service that scores predictions after official results arrive.
 *
 * Match prediction points:
 *   - Group stage, exact score          → 3 pts
 *   - Group stage, correct outcome only → 1 pt
 *   - Knockout stage, exact score       → 6 pts
 *   - Knockout stage, wrong             → 0 pts
 *
 * Group bonus prediction points (position-aware):
 *   - 1st + 2nd both correct            → 6 pts
 *   - Exactly one position correct      → 2 pts
 */
@Service
public class MatchPredictionScoringService {

    private static final Logger log = LoggerFactory.getLogger(MatchPredictionScoringService.class);

    private final MatchPredictionRepository repository;
    private final ParticipationRepository   participationRepository;

    public MatchPredictionScoringService(MatchPredictionRepository repository,
                                         ParticipationRepository participationRepository) {
        this.repository              = repository;
        this.participationRepository = participationRepository;
    }

    // ─── Match predictions ────────────────────────────────────────────────────

    @Transactional
    public void scoreMatchPredictions(UUID matchId,
                                      EventTypes.Score officialScore,
                                      EventTypes.MatchRound round) {
        List<MatchPrediction> predictions = repository.findAllByMatch(matchId);
        int scored = 0;
        for (MatchPrediction p : predictions) {
            if (p.getStatus() != MatchPrediction.Status.LOCKED) continue;
            int points = computeMatchPoints(p.getPredictedScore(), officialScore, round);
            p.score(points);
            repository.save(p);

            // Accumulate points on the Participation
            if (points > 0) {
                participationRepository
                        .findByGroupAndPlayer(p.getBettingGroupId(), p.getPlayerId())
                        .ifPresent(participation -> {
                            participation.addPoints(points);
                            participationRepository.save(participation);
                        });
            }
            scored++;
        }
        log.info("Scored {} predictions for match {} — result {}:{} ({})",
                scored, matchId, officialScore.goalsHome(), officialScore.goalsAway(), round);
    }

    // ─── Group bonus predictions ──────────────────────────────────────────────

    @Transactional
    public void scoreGroupBonusPredictions(String tournamentGroupId,
                                           String firstTeamId,
                                           String secondTeamId) {
        repository.scoreGroupBonusPredictions(tournamentGroupId, firstTeamId, secondTeamId);
        log.info("Scored group bonus predictions for group {} — 1st={}, 2nd={}",
                tournamentGroupId, firstTeamId, secondTeamId);
    }

    // ─── Points calculation (package-visible for tests) ───────────────────────

    static int computeMatchPoints(PredictedScore predicted,
                                   EventTypes.Score official,
                                   EventTypes.MatchRound round) {
        if (predicted == null) return 0;

        boolean exact = predicted.goalsHome() == official.goalsHome()
                     && predicted.goalsAway() == official.goalsAway();

        if (round == EventTypes.MatchRound.GROUP_STAGE) {
            if (exact) return 3;
            if (outcomeMatches(predicted, official)) return 1;
            return 0;
        } else {
            // All knockout rounds: only exact score rewarded
            return exact ? 6 : 0;
        }
    }

    private static boolean outcomeMatches(PredictedScore p, EventTypes.Score s) {
        return Integer.compare(p.goalsHome(), p.goalsAway())
            == Integer.compare(s.goalsHome(), s.goalsAway());
    }
}
