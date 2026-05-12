package dev.tippspiel.tournament.infrastructure.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.tippspiel.tournament.domain.model.TeamRef;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Collections;
import java.util.List;

/**
 * JPA converter: List&lt;TeamRef&gt; ↔ JSON string stored in a JSONB column.
 */
@Converter
class TeamRefListConverter implements AttributeConverter<List<TeamRef>, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<List<TeamRef>> TYPE = new TypeReference<>() {};

    @Override
    public String convertToDatabaseColumn(List<TeamRef> teams) {
        if (teams == null || teams.isEmpty()) return "[]";
        try {
            return MAPPER.writeValueAsString(teams);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize TeamRef list", e);
        }
    }

    @Override
    public List<TeamRef> convertToEntityAttribute(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return MAPPER.readValue(json, TYPE);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize TeamRef list", e);
        }
    }
}
