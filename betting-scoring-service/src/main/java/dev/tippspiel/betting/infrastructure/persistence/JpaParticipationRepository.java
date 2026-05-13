package dev.tippspiel.betting.infrastructure.persistence;

import dev.tippspiel.betting.domain.model.Participation;
import dev.tippspiel.betting.domain.repository.ParticipationRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaParticipationRepository implements ParticipationRepository {

    private final ParticipationJpaRepository jpaRepo;

    public JpaParticipationRepository(ParticipationJpaRepository jpaRepo) {
        this.jpaRepo = jpaRepo;
    }

    @Override
    public Optional<Participation> findByGroupAndPlayer(UUID bettingGroupId, UUID playerId) {
        return jpaRepo.findByBettingGroupIdAndPlayerId(bettingGroupId, playerId)
                .map(this::toDomain);
    }

    @Override
    public List<Participation> findAllByGroup(UUID bettingGroupId) {
        return jpaRepo.findAllByBettingGroupIdOrderByTotalPointsDesc(bettingGroupId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Participation save(Participation participation) {
        return toDomain(jpaRepo.save(toEntity(participation)));
    }

    @Override
    public List<Participation> saveAll(List<Participation> participations) {
        List<ParticipationJpaEntity> entities = participations.stream()
                .map(this::toEntity)
                .toList();
        return jpaRepo.saveAll(entities).stream()
                .map(this::toDomain)
                .toList();
    }

    // ─── Mapping ──────────────────────────────────────────────────────────────

    private Participation toDomain(ParticipationJpaEntity e) {
        return Participation.reconstitute(
                e.id,
                e.bettingGroupId,
                e.playerId,
                e.displayName,
                e.totalPoints,
                e.stakePaid,
                e.approved,
                e.prizeAmount
        );
    }

    private ParticipationJpaEntity toEntity(Participation p) {
        ParticipationJpaEntity e = new ParticipationJpaEntity();
        e.id             = p.getId();
        e.bettingGroupId = p.getBettingGroupId();
        e.playerId       = p.getPlayerId();
        e.displayName    = p.getDisplayName();
        e.totalPoints    = p.getTotalPoints();
        e.stakePaid      = p.isStakePaid();
        e.approved       = p.isApproved();
        e.prizeAmount    = p.getPrizeAmount();
        return e;
    }
}
