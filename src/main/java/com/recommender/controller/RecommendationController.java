package com.recommender.controller;

import com.recommender.dto.RecommendationRequest;
import com.recommender.model.Recommendation;
import com.recommender.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * REST controller for recommendations.
 * Provides endpoints for retrieving recommendations.
 */
@RestController
@RequestMapping("/api/recommendations")
@Slf4j
@RequiredArgsConstructor
public class RecommendationController {
    
    private final RecommendationService recommendationService;
    
    /**
     * Gets recommendations for a user.
     * 
     * @param request The recommendation request
     * @return The recommendations for the user
     */
    @PostMapping
    public ResponseEntity<Recommendation> getRecommendations(@Valid @RequestBody RecommendationRequest request) {
        log.info("Received recommendation request: {}", request);
        
        Recommendation recommendation = recommendationService.getRecommendations(request);
        
        return new ResponseEntity<>(recommendation, HttpStatus.OK);
    }
    
    /**
     * Gets recommendations for a user.
     * Convenience endpoint for getting recommendations with query parameters.
     * 
     * @param userId The user ID
     * @param limit The maximum number of recommendations to return
     * @param includeExplanations Whether to include explanations in the response
     * @return The recommendations for the user
     */
    @GetMapping
    public ResponseEntity<Recommendation> getRecommendations(
            @RequestParam String userId,
            @RequestParam(required = false, defaultValue = "10") int limit,
            @RequestParam(required = false, defaultValue = "true") boolean includeExplanations,
            @RequestParam(required = false) String contextId,
            @RequestParam(required = false) String experimentId) {
        
        RecommendationRequest request = RecommendationRequest.builder()
                .userId(userId)
                .limit(limit)
                .includeExplanations(includeExplanations)
                .contextId(contextId)
                .experimentId(experimentId)
                .build();
        
        Recommendation recommendation = recommendationService.getRecommendations(request);
        
        return new ResponseEntity<>(recommendation, HttpStatus.OK);
    }
    
    /**
     * Gets recommendations for a user in a specific context.
     * 
     * @param userId The user ID
     * @param contextId The context ID
     * @return The recommendations for the user in the specified context
     */
    @GetMapping("/context/{contextId}")
    public ResponseEntity<Recommendation> getRecommendationsForContext(
            @RequestParam String userId,
            @PathVariable String contextId,
            @RequestParam(required = false, defaultValue = "10") int limit,
            @RequestParam(required = false, defaultValue = "true") boolean includeExplanations) {
        
        RecommendationRequest request = RecommendationRequest.builder()
                .userId(userId)
                .contextId(contextId)
                .limit(limit)
                .includeExplanations(includeExplanations)
                .build();
        
        Recommendation recommendation = recommendationService.getRecommendations(request);
        
        return new ResponseEntity<>(recommendation, HttpStatus.OK);
    }
}
