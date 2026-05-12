package dev.tippspiel.betting.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface BettingGroupJpaRepository extends JpaRepository<BettingGroupJpaEntity, UUID> {}
