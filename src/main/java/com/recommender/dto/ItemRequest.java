package com.recommender.dto;

import com.recommender.model.Item;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.Map;
import java.util.Set;

/**
 * DTO for receiving item data from API clients.
 * This is used when clients want to add or update items in the recommendation system.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemRequest {
    
    @NotBlank(message = "Item ID is required")
    private String id;
    
    @NotBlank(message = "Item name is required")
    private String name;
    
    private String description;
    
    private Set<String> categories;
    
    private Map<String, Double> features;
    
    private Map<String, String> metadata;
    
    private double popularity;
    
    /**
     * Converts this DTO to an Item model object.
     * 
     * @return A new Item instance
     */
    public Item toItem() {
        long currentTime = System.currentTimeMillis();
        
        return Item.builder()
                .id(id)
                .name(name)
                .description(description)
                .categories(categories)
                .features(features)
                .metadata(metadata)
                .popularity(popularity)
                .creationTimestamp(currentTime)
                .lastUpdateTimestamp(currentTime)
                .build();
    }
}
