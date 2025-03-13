package com.recommender.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents a user profile in the recommendation system.
 * Contains user preferences and interaction history used for generating recommendations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class UserProfile {
    private String userId;
    private Map<String, Double> categoryPreferences;  // Category -> preference score
    private Map<String, Double> featurePreferences;   // Feature -> preference score
    private Set<String> recentlyViewedItems;          // Recently viewed item IDs
    private Set<String> purchasedItems;               // Purchased item IDs
    private Map<String, Double> itemRatings;          // ItemId -> rating score
    private long lastActivityTimestamp;               // Last user activity timestamp
    
    /**
     * Updates the user profile with a new user event.
     * This method is used to keep the user profile up-to-date with recent user activity.
     * 
     * @param event The user event to incorporate into the profile
     */
    public void updateWithEvent(UserEvent event) {
        // Initialize collections if they're null
        if (categoryPreferences == null) categoryPreferences = new HashMap<>();
        if (featurePreferences == null) featurePreferences = new HashMap<>();
        if (recentlyViewedItems == null) recentlyViewedItems = new HashSet<>();
        if (purchasedItems == null) purchasedItems = new HashSet<>();
        if (itemRatings == null) itemRatings = new HashMap<>();
        
        // Update last activity timestamp
        lastActivityTimestamp = event.getTimestamp().toEpochMilli();
        
        // Process event based on type
        switch (event.getEventType()) {
            case VIEW:
                recentlyViewedItems.add(event.getItemId());
                break;
            case CLICK:
                // Track clicks, could update a click count map if needed
                break;
            case ADD_TO_CART:
                // Track items added to cart
                break;
            case PURCHASE:
                purchasedItems.add(event.getItemId());
                break;
            case RATE:
                if (event.getScore() != null) {
                    itemRatings.put(event.getItemId(), event.getScore());
                }
                break;
            case SEARCH:
                // Track search terms if available in contextInfo
                break;
            case LIKE:
                // Track liked items
                break;
            case DISLIKE:
                // Track disliked items
                break;
            default:
                log.warn("Unhandled event type: {}", event.getEventType());
        }
    }
}
