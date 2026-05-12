package dev.tippspiel.betting.infrastructure.kafka;

import dev.tippspiel.events.tournament.TournamentManagementEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.Map;

@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, TournamentManagementEvent> tournamentEventConsumerFactory() {
        JsonDeserializer<TournamentManagementEvent> deserializer =
                new JsonDeserializer<>(TournamentManagementEvent.class, false);
        deserializer.addTrustedPackages("dev.tippspiel.events.tournament");

        return new DefaultKafkaConsumerFactory<>(
                Map.of(
                    ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                    ConsumerConfig.GROUP_ID_CONFIG,          "betting-scoring-service",
                    ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"
                ),
                new StringDeserializer(),
                deserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, TournamentManagementEvent>
            tournamentEventListenerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, TournamentManagementEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(tournamentEventConsumerFactory());
        return factory;
    }
}
