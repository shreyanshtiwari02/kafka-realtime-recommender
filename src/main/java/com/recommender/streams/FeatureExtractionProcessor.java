package com.recommender.streams;

import com.recommender.config.KafkaConfig;
import com.recommender.model.Item;
import com.recommender.model.UserEvent;
import com.recommender.utils.JsonSerde;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Extracts features from user events and item data for use in recommendation algorithms.
 * Uses Kafka Streams for real-time processing.
 */
@Component
@Slf4j
public class FeatureExtractionProcessor {

    /**
     * Configures the Kafka Streams topology for feature extraction.
     * 
     * @param streamsBuilder The streams builder to configure
     * @return The configured streams builder
     */
    @Autowired
    public void buildPipeline(StreamsBuilder streamsBuilder) {
        log.info("Configuring Kafka Streams for feature extraction");
        
        // Create serdes for our model classes
        JsonSerde<UserEvent> userEventSerde = new JsonSerde<>(UserEvent.class);
        JsonSerde<Item> itemSerde = new JsonSerde<>(Item.class);
        
        // Load items as a KTable for lookups
        KTable<String, Item> items = streamsBuilder
                .table(KafkaConfig.ITEMS_TOPIC, 
                       Consumed.with(Serdes.String(), itemSerde));
        
        // Stream of user events
        KStream<String, UserEvent> userEvents = streamsBuilder
                .stream(KafkaConfig.USER_EVENTS_TOPIC, 
                        Consumed.with(Serdes.String(), userEventSerde));
        
        // Join user events with item data to extract features
        KStream<String, Map<String, Double>> extractedFeatures = userEvents
                .filter((key, event) -> event != null && event.getItemId() != null)
                .selectKey((key, event) -> event.getItemId())
                .join(
                    items,
                    (event, item) -> {
                        // Extract features from the user event and item
                        Map<String, Double> features = new HashMap<>();
                        
                        // Add event-based features
                        features.put("event_score", calculateEventScore(event));
                        
                        // Add item-based features if available
                        if (item != null && item.getFeatures() != null) {
                            // Copy item features with a prefix
                            item.getFeatures().forEach((featureKey, featureValue) -> 
                                features.put("item_" + featureKey, featureValue));
                            
                            // Add category information as features
                            if (item.getCategories() != null) {
                                item.getCategories().forEach(category -> 
                                    features.put("category_" + category, 1.0));
                            }
                            
                            // Add popularity as a feature
                            features.put("item_popularity", item.getPopularity());
                        }
                        
                        // Add temporal features
                        features.put("recency", (double) (System.currentTimeMillis() - 
                                                         event.getTimestamp().toEpochMilli()));
                        
                        return features;
                    },
                    Joined.with(Serdes.String(), userEventSerde, itemSerde)
                )
                .selectKey((itemId, features) -> "feature_" + itemId);
        
        // Log extracted features for debugging
        extractedFeatures.peek((key, features) -> 
                log.debug("Extracted features for {}: {}", key, features));
        
        // Here we could output the extracted features to a topic for use in ML models
        // This is just a placeholder - in a real system you would likely use these
        // features to train or update ML models
    }
    
    /**
     * Calculates a score for a user event based on its type.
     * 
     * @param event The user event
     * @return A score value
     */
    private double calculateEventScore(UserEvent event) {
        Map<UserEvent.EventType, Double> eventScores = new HashMap<>();
        eventScores.put(UserEvent.EventType.VIEW, 1.0);
        eventScores.put(UserEvent.EventType.CLICK, 2.0);
        eventScores.put(UserEvent.EventType.ADD_TO_CART, 3.0);
        eventScores.put(UserEvent.EventType.PURCHASE, 5.0);
        eventScores.put(UserEvent.EventType.RATE, 4.0);
        eventScores.put(UserEvent.EventType.SEARCH, 0.5);
        eventScores.put(UserEvent.EventType.LIKE, 2.5);
        eventScores.put(UserEvent.EventType.DISLIKE, -1.0);
        
        return eventScores.getOrDefault(event.getEventType(), 0.0);
    }
}
