package com.recommender.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Represents a user interaction event in the system.
 * These events are used to track user behavior for recommendation purposes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEvent {
    private String userId;
    private String itemId;
    private EventType eventType;
    private Instant timestamp;
    private Double score;
    private String sessionId;
    private String contextInfo;
    
    /**
     * Types of user interaction events.
     */
    public enum EventType {
        VIEW,       // User viewed an item
        CLICK,      // User clicked on an item
        ADD_TO_CART,// User added item to cart
        PURCHASE,   // User purchased an item
        RATE,       // User rated an item
        SEARCH,     // User searched for something
        LIKE,       // User liked an item
        DISLIKE     // User disliked an item
    }
}
