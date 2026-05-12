package dev.tippspiel.tournament.application.command;

import dev.tippspiel.events.tournament.*;
import dev.tippspiel.tournament.domain.model.*;
import dev.tippspiel.tournament.domain.repository.MatchRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class MatchCommandService {

    private static final String TOPIC = "tippspiel.tournament-management.events";

    private final MatchRepository matchRepository;
    private final KafkaTemplate<String, TournamentManagementEvent> kafka;

    public MatchCommandService(MatchRepository matchRepository,
                               KafkaTemplate<String, TournamentManagementEvent> kafka) {
        this.matchRepository = matchRepository;
        this.kafka           = kafka;
    }

    @Transactional
    public UUID scheduleMatch(ScheduleMatchCommand cmd) {
        Match match = Match.schedule(
            cmd.matchId(),
            cmd.round(),
            cmd.groupId(),
            cmd.homePlaceholder(),
            cmd.awayPlaceholder(),
            cmd.scheduledFor()
        );
        matchRepository.save(match);

        kafka.send(TOPIC, match.getId().toString(),
            new MatchScheduled(
                UUID.randomUUID(), Instant.now(),
                match.getId(), toEventRound(match.getRound()),
                match.getGroupId(),
                match.getHomePlaceholder(), match.getAwayPlaceholder(),
                match.getScheduledFor()
            ));

        return match.getId();
    }

    @Transactional
    public void startMatch(StartMatchCommand cmd) {
        Match match = matchRepository.findById(cmd.matchId())
            .orElseThrow(() -> new IllegalArgumentException("Match not found: " + cmd.matchId()));

        boolean wasFirstOfGroup = isFirstMatchOfGroup(match);
        match.start();
        matchRepository.save(match);

        kafka.send(TOPIC, match.getId().toString(),
            new MatchStarted(
                UUID.randomUUID(), Instant.now(),
                match.getId(), toEventRound(match.getRound()),
                match.getGroupId(),
                wasFirstOfGroup
            ));
    }

    @Transactional
    public void finishMatch(FinishMatchCommand cmd) {
        Match match = matchRepository.findById(cmd.matchId())
            .orElseThrow(() -> new IllegalArgumentException("Match not found: " + cmd.matchId()));

        Score score = new Score(cmd.goalsHome(), cmd.goalsAway());
        match.finish(score, cmd.phase());
        matchRepository.save(match);

        kafka.send(TOPIC, match.getId().toString(),
            new MatchFinished(
                UUID.randomUUID(), Instant.now(),
                match.getId(), toEventRound(match.getRound()),
                match.getGroupId(),
                toEventTeam(match.getHomeTeam()),
                toEventTeam(match.getAwayTeam()),
                new dev.tippspiel.events.tournament.EventTypes.Score(cmd.goalsHome(), cmd.goalsAway()),
                toEventPhase(cmd.phase())
            ));
    }

    @Transactional
    public void resolveFixture(ResolveMatchFixtureCommand cmd) {
        Match match = matchRepository.findById(cmd.matchId())
            .orElseThrow(() -> new IllegalArgumentException("Match not found: " + cmd.matchId()));

        match.resolveFixture(cmd.placeholder(), cmd.team(), cmd.side());
        matchRepository.save(match);

        kafka.send(TOPIC, match.getId().toString(),
            new MatchFixtureResolved(
                UUID.randomUUID(), Instant.now(),
                match.getId(),
                cmd.placeholder(),
                toEventTeam(cmd.team()),
                toEventSide(cmd.side())
            ));
    }

    // --- Mapping helpers (domain model → shared event types) ---

    private boolean isFirstMatchOfGroup(Match match) {
        // Delegate to repository: true if no other STARTED/FINISHED match exists for this group
        // Simplified here — full implementation in MatchRepository
        return match.isGroupStageMatch() && matchRepository.isFirstStartedInGroup(match.getGroupId(), match.getId());
    }

    private static EventTypes.MatchRound toEventRound(MatchRound r) {
        return EventTypes.MatchRound.valueOf(r.name());
    }

    private static EventTypes.MatchPhase toEventPhase(MatchPhase p) {
        return EventTypes.MatchPhase.valueOf(p.name());
    }

    private static EventTypes.TeamRef toEventTeam(TeamRef t) {
        if (t == null) return null;
        return new EventTypes.TeamRef(t.teamId(), t.name());
    }

    private static EventTypes.Side toEventSide(Side s) {
        return EventTypes.Side.valueOf(s.name());
    }
}
