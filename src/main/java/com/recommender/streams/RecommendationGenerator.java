package com.recommender.streams;

import com.recommender.config.KafkaConfig;
import com.recommender.model.Item;
import com.recommender.model.Recommendation;
import com.recommender.model.UserProfile;
import com.recommender.utils.JsonSerde;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Generates recommendations in real-time based on user profiles and item data.
 * Uses Kafka Streams for processing.
 */
@Component
@Slf4j
public class RecommendationGenerator {

    private static final String MODEL_VERSION = "1.0.0";
    private static final int MAX_RECOMMENDATIONS = 10;

    /**
     * Configures the Kafka Streams topology for generating recommendations.
     * 
     * @param streamsBuilder The streams builder to configure
     * @return The configured streams builder
     */
    @Autowired
    public void buildPipeline(StreamsBuilder streamsBuilder) {
        log.info("Configuring Kafka Streams for recommendation generation");
        
        // Create serdes for our model classes
        JsonSerde<UserProfile> userProfileSerde = new JsonSerde<>(UserProfile.class);
        JsonSerde<Item> itemSerde = new JsonSerde<>(Item.class);
        JsonSerde<Recommendation> recommendationSerde = new JsonSerde<>(Recommendation.class);
        
        // Load user profiles as a KTable
        KTable<String, UserProfile> userProfiles = streamsBuilder
                .table(KafkaConfig.USER_PROFILES_TOPIC, 
                       Consumed.with(Serdes.String(), userProfileSerde));
        
        // Load items as a KTable
        KTable<String, Item> items = streamsBuilder
                .table(KafkaConfig.ITEMS_TOPIC, 
                       Consumed.with(Serdes.String(), itemSerde));
        
        // Generate recommendations when user profiles are updated
        KStream<String, Recommendation> recommendations = userProfiles
                .toStream()
                .mapValues((userId, userProfile) -> {
                    // Generate a unique recommendation ID
                    String recommendationId = UUID.randomUUID().toString();
                    
                    // Get all items (in a real system, you would filter and rank more efficiently)
                    Map<String, Item> allItems = new HashMap<>();
                    items.toStream().foreach((itemId, item) -> {
                        if (item != null) {
                            allItems.put(itemId, item);
                        }
                    });
                    
                    // Generate recommendations based on user profile and items
                    List<Recommendation.RecommendedItem> recommendedItems = generateRecommendations(
                            userProfile, allItems, MAX_RECOMMENDATIONS);
                    
                    // Create and return the recommendation
                    return Recommendation.builder()
                            .id(recommendationId)
                            .userId(userId)
                            .timestamp(Instant.now())
                            .contextId("homepage")  // Default context
                            .items(recommendedItems)
                            .modelVersion(MODEL_VERSION)
                            .build();
                });
        
        // Output recommendations to a topic
        recommendations
                .peek((userId, recommendation) -> 
                        log.debug("Generated {} recommendations for user {}", 
                                recommendation.getItems().size(), userId))
                .to(KafkaConfig.RECOMMENDATIONS_TOPIC, 
                    Produced.with(Serdes.String(), recommendationSerde));
    }
    
    /**
     * Generates recommendations for a user based on their profile and available items.
     * 
     * @param userProfile The user's profile
     * @param items Available items
     * @param maxRecommendations Maximum number of recommendations to generate
     * @return List of recommended items
     */
    private List<Recommendation.RecommendedItem> generateRecommendations(
            UserProfile userProfile, Map<String, Item> items, int maxRecommendations) {
        
        // Skip items the user has already purchased
        Set<String> excludeItems = userProfile.getPurchasedItems() != null ? 
                userProfile.getPurchasedItems() : new HashSet<>();
        
        // Calculate scores for each item
        Map<String, Double> itemScores = new HashMap<>();
        
        items.forEach((itemId, item) -> {
            if (!excludeItems.contains(itemId)) {
                // Calculate a score based on multiple factors
                double score = calculateItemScore(userProfile, item);
                itemScores.put(itemId, score);
            }
        });
        
        // Sort items by score and take the top N
        return itemScores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(maxRecommendations)
                .map(entry -> {
                    String itemId = entry.getKey();
                    double score = entry.getValue();
                    Item item = items.get(itemId);
                    
                    // Create score components for explanation
                    Map<String, Double> scoreComponents = new HashMap<>();
                    if (userProfile.getCategoryPreferences() != null && item.getCategories() != null) {
                        double categoryScore = calculateCategoryScore(userProfile, item);
                        scoreComponents.put("category_match", categoryScore);
                    }
                    scoreComponents.put("popularity", item.getPopularity() * 0.3);
                    
                    // Generate explanation
                    String explanation = generateExplanation(userProfile, item, scoreComponents);
                    
                    return Recommendation.RecommendedItem.builder()
                            .itemId(itemId)
                            .score(score)
                            .scoreComponents(scoreComponents)
                            .explanation(explanation)
                            .build();
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Calculates a score for an item based on the user profile.
     * 
     * @param userProfile The user's profile
     * @param item The item to score
     * @return A score value
     */
    private double calculateItemScore(UserProfile userProfile, Item item) {
        double score = 0.0;
        
        // Add popularity component
        score += item.getPopularity() * 0.3;
        
        // Add category preference component
        if (userProfile.getCategoryPreferences() != null && item.getCategories() != null) {
            score += calculateCategoryScore(userProfile, item) * 0.4;
        }
        
        // Add recency component (newer items get a boost)
        long currentTime = System.currentTimeMillis();
        long itemAge = currentTime - item.getCreationTimestamp();
        double recencyScore = Math.max(0, 1.0 - (itemAge / (30.0 * 24 * 60 * 60 * 1000))); // 30 days max
        score += recencyScore * 0.3;
        
        return score;
    }
    
    /**
     * Calculates a category match score between user preferences and item categories.
     * 
     * @param userProfile The user's profile
     * @param item The item to score
     * @return A category match score
     */
    private double calculateCategoryScore(UserProfile userProfile, Item item) {
        double categoryScore = 0.0;
        int matches = 0;
        
        for (String category : item.getCategories()) {
            if (userProfile.getCategoryPreferences().containsKey(category)) {
                categoryScore += userProfile.getCategoryPreferences().get(category);
                matches++;
            }
        }
        
        return matches > 0 ? categoryScore / matches : 0.0;
    }
    
    /**
     * Generates a human-readable explanation for a recommendation.
     * 
     * @param userProfile The user's profile
     * @param item The recommended item
     * @param scoreComponents The components of the recommendation score
     * @return An explanation string
     */
    private String generateExplanation(UserProfile userProfile, Item item, 
                                      Map<String, Double> scoreComponents) {
        // Find the highest scoring component
        Map.Entry<String, Double> highestComponent = scoreComponents.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);
        
        if (highestComponent != null) {
            switch (highestComponent.getKey()) {
                case "category_match":
                    return "Based on your interest in " + 
                           String.join(", ", item.getCategories());
                case "popularity":
                    return "Popular among other users";
                default:
                    return "Recommended for you";
            }
        }
        
        return "Recommended for you";
    }
}
