package com.recommender.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Represents a recommendation generated for a specific user.
 * Contains a list of recommended items with scores and explanation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Recommendation {
    private String id;
    private String userId;
    private Instant timestamp;
    private String contextId;     // Context in which recommendation was generated (e.g., homepage, product page)
    private List<RecommendedItem> items;
    private String modelVersion;  // Version of the model that generated this recommendation
    private String experimentId;  // A/B test experiment ID if applicable
    
    /**
     * Represents a single item in a recommendation with its score and explanation.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendedItem {
        private String itemId;
        private double score;
        private Map<String, Double> scoreComponents;  // Breakdown of the score (e.g., content-based: 0.7, collaborative: 0.3)
        private String explanation;                   // Human-readable explanation of why this item was recommended
    }
}
