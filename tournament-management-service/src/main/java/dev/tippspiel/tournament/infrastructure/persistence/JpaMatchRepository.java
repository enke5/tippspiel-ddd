package dev.tippspiel.tournament.infrastructure.persistence;

import dev.tippspiel.tournament.domain.model.*;
import dev.tippspiel.tournament.domain.repository.MatchRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaMatchRepository implements MatchRepository {

    private final MatchJpaRepository springData;

    public JpaMatchRepository(MatchJpaRepository springData) {
        this.springData = springData;
    }

    @Override
    public Optional<Match> findById(UUID id) {
        return springData.findById(id).map(this::toDomain);
    }

    @Override
    public Match save(Match match) {
        MatchJpaEntity saved = springData.save(toEntity(match));
        return toDomain(saved);
    }

    @Override
    public boolean isFirstStartedInGroup(String groupId, UUID excludeMatchId) {
        return springData.countStartedOrFinishedInGroup(groupId, excludeMatchId) == 0;
    }

    // ─── Mapping ──────────────────────────────────────────────────────────────

    private Match toDomain(MatchJpaEntity e) {
        TeamRef homeTeam = (e.homeTeamId != null)
                ? new TeamRef(e.homeTeamId, e.homeTeamName)
                : null;
        TeamRef awayTeam = (e.awayTeamId != null)
                ? new TeamRef(e.awayTeamId, e.awayTeamName)
                : null;
        Score score = (e.goalsHome != null && e.goalsAway != null)
                ? new Score(e.goalsHome, e.goalsAway)
                : null;
        MatchPhase phase = (e.phase != null) ? MatchPhase.valueOf(e.phase) : null;

        return Match.reconstitute(
                e.id,
                MatchRound.valueOf(e.round),
                e.groupId,
                e.homePlaceholder,
                e.awayPlaceholder,
                homeTeam,
                awayTeam,
                e.scheduledFor,
                Match.Status.valueOf(e.status),
                score,
                phase
        );
    }

    private MatchJpaEntity toEntity(Match m) {
        MatchJpaEntity e = new MatchJpaEntity();
        e.id              = m.getId();
        e.round           = m.getRound().name();
        e.groupId         = m.getGroupId();
        e.homePlaceholder = m.getHomePlaceholder();
        e.awayPlaceholder = m.getAwayPlaceholder();
        e.homeTeamId      = m.getHomeTeam() != null ? m.getHomeTeam().teamId() : null;
        e.homeTeamName    = m.getHomeTeam() != null ? m.getHomeTeam().name()   : null;
        e.awayTeamId      = m.getAwayTeam() != null ? m.getAwayTeam().teamId() : null;
        e.awayTeamName    = m.getAwayTeam() != null ? m.getAwayTeam().name()   : null;
        e.scheduledFor    = m.getScheduledFor();
        e.status          = m.getStatus().name();
        e.goalsHome       = m.getScore() != null ? m.getScore().goalsHome() : null;
        e.goalsAway       = m.getScore() != null ? m.getScore().goalsAway() : null;
        e.phase           = m.getPhase() != null ? m.getPhase().name()      : null;
        return e;
    }
}
