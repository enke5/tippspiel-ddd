package dev.tippspiel.events.tournament;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.Instant;
import java.util.UUID;

/**
 * Marker interface for all domain events published by Tournament Management.
 * These are the integration events consumed by Betting & Scoring via Kafka.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = MatchScheduled.class,              name = "MatchScheduled"),
    @JsonSubTypes.Type(value = MatchStarted.class,                name = "MatchStarted"),
    @JsonSubTypes.Type(value = MatchFinished.class,               name = "MatchFinished"),
    @JsonSubTypes.Type(value = MatchFixtureResolved.class,        name = "MatchFixtureResolved"),
    @JsonSubTypes.Type(value = TournamentGroupCompleted.class,    name = "TournamentGroupCompleted"),
    @JsonSubTypes.Type(value = AllGroupStandingsCalculated.class, name = "AllGroupStandingsCalculated"),
    @JsonSubTypes.Type(value = BestThirdPlaceDetermined.class,    name = "BestThirdPlaceDetermined"),
    @JsonSubTypes.Type(value = TournamentStarted.class,           name = "TournamentStarted")
})
public sealed interface TournamentManagementEvent
    permits MatchScheduled, MatchStarted, MatchFinished, MatchFixtureResolved,
            TournamentGroupCompleted, AllGroupStandingsCalculated,
            BestThirdPlaceDetermined, TournamentStarted {

    /** Unique event ID for idempotency checks. */
    UUID eventId();

    /** When this event was emitted. */
    Instant occurredAt();
}
