package dev.tippspiel.betting.infrastructure.persistence;

import dev.tippspiel.betting.domain.model.BettingGroup;
import dev.tippspiel.betting.domain.model.Stake;
import dev.tippspiel.betting.domain.repository.BettingGroupRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaBettingGroupRepository implements BettingGroupRepository {

    private final BettingGroupJpaRepository jpaRepo;

    public JpaBettingGroupRepository(BettingGroupJpaRepository jpaRepo) {
        this.jpaRepo = jpaRepo;
    }

    @Override
    public Optional<BettingGroup> findById(UUID id) {
        return jpaRepo.findById(id).map(this::toDomain);
    }

    @Override
    public List<BettingGroup> findAllByTournamentRef(String tournamentRef) {
        return jpaRepo.findAllByTournamentRef(tournamentRef).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public BettingGroup save(BettingGroup group) {
        BettingGroupJpaEntity saved = jpaRepo.save(toEntity(group));
        return toDomain(saved);
    }

    // ─── Mapping ──────────────────────────────────────────────────────────────

    private BettingGroup toDomain(BettingGroupJpaEntity e) {
        return BettingGroup.reconstitute(
                e.id,
                e.name,
                Stake.of(e.stakeAmount, e.stakeCurrency),
                e.tournamentRef,
                BettingGroup.Status.valueOf(e.status)
        );
    }

    private BettingGroupJpaEntity toEntity(BettingGroup g) {
        BettingGroupJpaEntity e = new BettingGroupJpaEntity();
        e.id            = g.getId();
        e.name          = g.getName();
        e.tournamentRef = g.getTournamentRef();
        e.stakeAmount   = g.getStake().amount();
        e.stakeCurrency = g.getStake().currency().getCurrencyCode();
        e.status        = g.getStatus().name();
        return e;
    }
}
