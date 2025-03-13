package com.recommender.streams;

import com.recommender.config.KafkaConfig;
import com.recommender.model.UserEvent;
import com.recommender.model.UserProfile;
import com.recommender.utils.JsonSerde;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Processes user events in real-time using Kafka Streams.
 * Extracts features from user events and updates user profiles.
 */
@Component
@Slf4j
public class UserEventProcessor {

    /**
     * Configures the Kafka Streams topology for processing user events.
     * 
     * @param streamsBuilder The streams builder to configure
     * @return The configured streams builder
     */
    @Autowired
    public void buildPipeline(StreamsBuilder streamsBuilder) {
        log.info("Configuring Kafka Streams for user event processing");
        
        // Create serdes for our model classes
        JsonSerde<UserEvent> userEventSerde = new JsonSerde<>(UserEvent.class);
        JsonSerde<UserProfile> userProfileSerde = new JsonSerde<>(UserProfile.class);
        
        // Stream of user events
        KStream<String, UserEvent> userEvents = streamsBuilder
                .stream(KafkaConfig.USER_EVENTS_TOPIC, 
                        Consumed.with(Serdes.String(), userEventSerde))
                .peek((key, event) -> log.debug("Processing user event: {}", event));
        
        // Extract features from user events
        KStream<String, UserEvent> enrichedEvents = userEvents
                .mapValues(event -> {
                    // Here we could enrich the event with additional information
                    // For example, calculate a score based on the event type
                    if (event.getScore() == null) {
                        double score = calculateEventScore(event);
                        event.setScore(score);
                    }
                    return event;
                });
        
        // Group events by user ID to update user profiles
        KTable<String, UserProfile> userProfiles = enrichedEvents
                .groupByKey()
                .aggregate(
                        UserProfile::new,  // Initializer
                        (userId, event, profile) -> {
                            // Update profile with new event
                            profile.setUserId(userId);
                            profile.updateWithEvent(event);
                            return profile;
                        },
                        Materialized.with(Serdes.String(), userProfileSerde)
                );
        
        // Output updated user profiles to a topic
        userProfiles.toStream()
                .peek((userId, profile) -> log.debug("Updated user profile for user: {}", userId))
                .to(KafkaConfig.USER_PROFILES_TOPIC, 
                    Produced.with(Serdes.String(), userProfileSerde));
        
        // Create a windowed view of recent user events for real-time analysis
        KTable<Windowed<String>, Long> userActivityCounts = userEvents
                .groupByKey()
                .windowedBy(TimeWindows.of(Duration.ofMinutes(10)))
                .count();
        
        // Log user activity for monitoring
        userActivityCounts.toStream()
                .peek((windowedUserId, count) -> 
                        log.debug("User {} had {} events in the last 10 minutes", 
                                windowedUserId.key(), count));
    }
    
    /**
     * Calculates a score for a user event based on its type.
     * This score can be used for weighting events in the recommendation algorithm.
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
