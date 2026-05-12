package dev.tippspiel.tournament.domain.model;

/** Value object: a team reference. Stable — teams do not change during a tournament. */
public record TeamRef(String teamId, String name) {

    public TeamRef {
        if (teamId == null || teamId.isBlank()) throw new IllegalArgumentException("teamId required");
        if (name == null || name.isBlank())     throw new IllegalArgumentException("name required");
    }
}
