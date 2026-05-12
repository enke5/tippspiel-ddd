package dev.tippspiel.tournament.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface TournamentGroupJpaRepository extends JpaRepository<TournamentGroupJpaEntity, UUID> {

    List<TournamentGroupJpaEntity> findAllByTournamentRefOrderByLabelAsc(String tournamentRef);
}
