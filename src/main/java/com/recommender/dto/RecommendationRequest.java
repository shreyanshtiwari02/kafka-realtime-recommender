package com.recommender.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

/**
 * DTO for receiving recommendation requests from API clients.
 * This is used when clients want to get recommendations for a specific user.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationRequest {
    
    @NotBlank(message = "User ID is required")
    private String userId;
    
    private String contextId;
    
    @Min(value = 1, message = "Minimum number of recommendations is 1")
    @Max(value = 100, message = "Maximum number of recommendations is 100")
    @Builder.Default
    private int limit = 10;
    
    private String experimentId;
    
    @Builder.Default
    private boolean includeExplanations = true;
}
