package com.recommender.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
            case PURCHASE:
                purchasedItems.add(event.getItemId());
                break;
            case RATE:
                if (event.getScore() != null) {
                    itemRatings.put(event.getItemId(), event.getScore());
                }
                break;
            // Other event types can be handled here
        }
    }
}
