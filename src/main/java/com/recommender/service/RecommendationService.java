package com.recommender.service;

import com.recommender.config.KafkaConfig;
import com.recommender.dto.RecommendationRequest;
import com.recommender.model.Recommendation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

/**
 * Service for handling recommendations.
 * Retrieves recommendations from the Kafka Streams state store.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RecommendationService {
    
    private final StreamsBuilderFactoryBean streamsBuilderFactoryBean;
    
    /**
     * Gets recommendations for a user.
     * 
     * @param request The recommendation request
     * @return The recommendations for the user
     */
    public Recommendation getRecommendations(RecommendationRequest request) {
        log.info("Getting recommendations for user: {}", request.getUserId());
        
        try {
            // Get the Kafka Streams instance
            KafkaStreams kafkaStreams = streamsBuilderFactoryBean.getKafkaStreams();
            if (kafkaStreams == null) {
                log.error("Kafka Streams is not available");
                return createEmptyRecommendation(request);
            }
            
            // Get the recommendations store
            ReadOnlyKeyValueStore<String, Recommendation> recommendationsStore = 
                    kafkaStreams.store(
                            StoreQueryParameters.fromNameAndType(
                                    "recommendations-store", 
                                    QueryableStoreTypes.keyValueStore()));
            
            // Get the recommendations for the user
            Recommendation recommendation = recommendationsStore.get(request.getUserId());
            
            if (recommendation == null) {
                log.warn("No recommendations found for user: {}", request.getUserId());
                return createEmptyRecommendation(request);
            }
            
            // Apply the limit if needed
            if (request.getLimit() < recommendation.getItems().size()) {
                recommendation.setItems(recommendation.getItems().subList(0, request.getLimit()));
            }
            
            return recommendation;
        } catch (Exception e) {
            log.error("Error getting recommendations for user: {}", request.getUserId(), e);
            return createEmptyRecommendation(request);
        }
    }
    
    /**
     * Creates an empty recommendation when no recommendations are available.
     * 
     * @param request The recommendation request
     * @return An empty recommendation
     */
    private Recommendation createEmptyRecommendation(RecommendationRequest request) {
        return Recommendation.builder()
                .id(UUID.randomUUID().toString())
                .userId(request.getUserId())
                .timestamp(Instant.now())
                .contextId(request.getContextId())
                .items(Collections.emptyList())
                .modelVersion("1.0.0")
                .experimentId(request.getExperimentId())
                .build();
    }
}
