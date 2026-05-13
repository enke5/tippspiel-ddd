package dev.tippspiel.betting.domain.repository;

import dev.tippspiel.betting.domain.model.BettingGroup;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BettingGroupRepository {

    Optional<BettingGroup> findById(UUID id);

    Optional<BettingGroup> findBySlug(String slug);

    List<BettingGroup> findAllByTournamentRef(String tournamentRef);

    BettingGroup save(BettingGroup group);
}
