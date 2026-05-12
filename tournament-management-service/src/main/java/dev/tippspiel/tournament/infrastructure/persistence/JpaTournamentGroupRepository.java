package dev.tippspiel.tournament.infrastructure.persistence;

import dev.tippspiel.tournament.domain.model.TournamentGroup;
import dev.tippspiel.tournament.domain.repository.TournamentGroupRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaTournamentGroupRepository implements TournamentGroupRepository {

    private final TournamentGroupJpaRepository springData;

    public JpaTournamentGroupRepository(TournamentGroupJpaRepository springData) {
        this.springData = springData;
    }

    @Override
    public Optional<TournamentGroup> findById(UUID id) {
        return springData.findById(id).map(this::toDomain);
    }

    @Override
    public List<TournamentGroup> findAllByTournamentRef(String tournamentRef) {
        return springData.findAllByTournamentRefOrderByLabelAsc(tournamentRef)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public TournamentGroup save(TournamentGroup group) {
        TournamentGroupJpaEntity saved = springData.save(toEntity(group));
        return toDomain(saved);
    }

    // ─── Mapping ──────────────────────────────────────────────────────────────

    private TournamentGroup toDomain(TournamentGroupJpaEntity e) {
        return TournamentGroup.reconstitute(
                e.id,
                e.label,
                e.tournamentRef,
                e.standingsPolicyName,
                e.teams,
                TournamentGroup.Status.valueOf(e.status),
                e.finalStandings
        );
    }

    private TournamentGroupJpaEntity toEntity(TournamentGroup g) {
        TournamentGroupJpaEntity e = new TournamentGroupJpaEntity();
        e.id                  = g.getId();
        e.label               = g.getLabel();
        e.tournamentRef       = g.getTournamentRef();
        e.standingsPolicyName = g.getStandingsPolicyName();
        e.teams               = g.getTeams();
        e.status              = g.getStatus().name();
        e.finalStandings      = g.getFinalStandings().isEmpty() ? null : g.getFinalStandings();
        return e;
    }
}
