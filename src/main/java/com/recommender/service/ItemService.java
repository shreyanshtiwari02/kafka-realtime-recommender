package com.recommender.service;

import com.recommender.config.KafkaConfig;
import com.recommender.model.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Service for handling items in the recommendation system.
 * Publishes items to Kafka for processing by the recommendation system.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ItemService {
    
    private final KafkaTemplate<String, Item> kafkaTemplate;
    
    /**
     * Publishes an item to Kafka.
     * 
     * @param item The item to publish
     * @return The published item
     */
    public Item publishItem(Item item) {
        log.info("Publishing item: {}", item);
        
        // Use the item ID as the key for the message
        kafkaTemplate.send(KafkaConfig.ITEMS_TOPIC, item.getId(), item);
        
        return item;
    }
    
    /**
     * Updates an existing item.
     * 
     * @param item The updated item
     * @return The updated item
     */
    public Item updateItem(Item item) {
        log.info("Updating item: {}", item);
        
        // Set the last update timestamp
        item.setLastUpdateTimestamp(System.currentTimeMillis());
        
        // Use the item ID as the key for the message
        kafkaTemplate.send(KafkaConfig.ITEMS_TOPIC, item.getId(), item);
        
        return item;
    }
}
