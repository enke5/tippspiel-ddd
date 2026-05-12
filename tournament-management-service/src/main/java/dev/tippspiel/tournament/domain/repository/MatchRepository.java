package dev.tippspiel.tournament.domain.repository;

import dev.tippspiel.tournament.domain.model.Match;

import java.util.Optional;
import java.util.UUID;

/** Domain repository interface — implemented in infrastructure layer. */
public interface MatchRepository {

    Optional<Match> findById(UUID id);

    Match save(Match match);

    /** Returns true if no other match in the given group has status STARTED or FINISHED yet. */
    boolean isFirstStartedInGroup(String groupId, UUID excludeMatchId);
}
