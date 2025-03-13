package com.recommender.service;

import com.recommender.config.KafkaConfig;
import com.recommender.model.UserEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Service for handling user events.
 * Publishes user events to Kafka for processing by the recommendation system.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserEventService {
    
    private final KafkaTemplate<String, UserEvent> kafkaTemplate;
    
    /**
     * Publishes a user event to Kafka.
     * 
     * @param userEvent The user event to publish
     * @return The published user event
     */
    public UserEvent publishUserEvent(UserEvent userEvent) {
        log.info("Publishing user event: {}", userEvent);
        
        // Use the user ID as the key for the message
        // This ensures that events for the same user go to the same partition
        kafkaTemplate.send(KafkaConfig.USER_EVENTS_TOPIC, userEvent.getUserId(), userEvent);
        
        return userEvent;
    }
}
