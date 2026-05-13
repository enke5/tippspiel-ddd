package dev.tippspiel.betting.domain.repository;

import dev.tippspiel.betting.domain.model.Participation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ParticipationRepository {

    Optional<Participation> findByGroupAndPlayer(UUID bettingGroupId, UUID playerId);

    /** All participants of a betting group, ordered by total_points descending. */
    List<Participation> findAllByGroup(UUID bettingGroupId);

    Participation save(Participation participation);

    /** Persist all at once (used after prize distribution). */
    List<Participation> saveAll(List<Participation> participations);
}
