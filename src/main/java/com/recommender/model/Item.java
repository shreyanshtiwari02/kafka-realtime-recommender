package com.recommender.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

/**
 * Represents an item in the recommendation system.
 * Items are the entities that will be recommended to users.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Item {
    private String id;
    private String name;
    private String description;
    private Set<String> categories;
    private Map<String, Double> features;  // Numerical features for ML models
    private Map<String, String> metadata;  // Additional metadata
    private double popularity;             // Popularity score
    private long creationTimestamp;        // When the item was created
    private long lastUpdateTimestamp;      // When the item was last updated
}
