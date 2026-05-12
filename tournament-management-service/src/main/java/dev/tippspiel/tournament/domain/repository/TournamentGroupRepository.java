package dev.tippspiel.tournament.domain.repository;

import dev.tippspiel.tournament.domain.model.TournamentGroup;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Domain repository interface — implemented in infrastructure layer. */
public interface TournamentGroupRepository {

    Optional<TournamentGroup> findById(UUID id);

    /** Find all groups for a given tournament, ordered by label. */
    List<TournamentGroup> findAllByTournamentRef(String tournamentRef);

    TournamentGroup save(TournamentGroup group);
}
