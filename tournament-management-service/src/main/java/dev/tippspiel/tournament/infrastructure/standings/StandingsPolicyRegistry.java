package dev.tippspiel.tournament.infrastructure.standings;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.tippspiel.tournament.domain.service.StandingsCriterion;
import dev.tippspiel.tournament.domain.service.StandingsPolicy;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Loads StandingsPolicy definitions from a JSON file.
 *
 * Default location: classpath:standings-policies.json
 * Override via: standings.policies.location=file:/path/to/custom-policies.json
 *
 * JSON format:
 * <pre>
 * [
 *   {
 *     "name": "UEFA_EURO",
 *     "criteria": ["POINTS", "HEAD_TO_HEAD_POINTS", ...]
 *   },
 *   ...
 * ]
 * </pre>
 */
@Component
public class StandingsPolicyRegistry {

    private static final Logger log = LoggerFactory.getLogger(StandingsPolicyRegistry.class);

    private final Resource policiesResource;
    private final ObjectMapper objectMapper;
    private Map<String, StandingsPolicy> policies;

    public StandingsPolicyRegistry(
            @Value("${standings.policies.location:classpath:standings-policies.json}") Resource policiesResource,
            ObjectMapper objectMapper) {
        this.policiesResource = policiesResource;
        this.objectMapper     = objectMapper;
    }

    @PostConstruct
    void load() throws IOException {
        PolicyDefinition[] defs = objectMapper.readValue(policiesResource.getInputStream(), PolicyDefinition[].class);
        policies = Arrays.stream(defs)
            .map(d -> new StandingsPolicy(d.name(), d.criteria().stream()
                .map(StandingsCriterion::valueOf)
                .collect(Collectors.toList())))
            .collect(Collectors.toMap(StandingsPolicy::name, p -> p));
        log.info("Loaded {} standings policies: {}", policies.size(), policies.keySet());
    }

    /** Returns the policy for the given name, or empty if not found. */
    public Optional<StandingsPolicy> findByName(String name) {
        return Optional.ofNullable(policies.get(name));
    }

    /** Returns the policy for the given name, throwing if not found. */
    public StandingsPolicy getByName(String name) {
        return findByName(name).orElseThrow(() ->
            new IllegalArgumentException("No standings policy found for name: '" + name +
                "'. Available: " + policies.keySet()));
    }

    // ─── JSON deserialization target ─────────────────────────────────────────

    private record PolicyDefinition(String name, List<String> criteria) {}
}
