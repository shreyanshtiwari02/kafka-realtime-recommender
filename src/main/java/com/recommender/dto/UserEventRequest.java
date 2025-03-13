package com.recommender.dto;

import com.recommender.model.UserEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Instant;

/**
 * DTO for receiving user event data from API clients.
 * This is used when clients want to record user interactions.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEventRequest {
    
    @NotBlank(message = "User ID is required")
    private String userId;
    
    @NotBlank(message = "Item ID is required")
    private String itemId;
    
    @NotNull(message = "Event type is required")
    private UserEvent.EventType eventType;
    
    private Double score;
    
    private String sessionId;
    
    private String contextInfo;
    
    /**
     * Converts this DTO to a UserEvent model object.
     * 
     * @return A new UserEvent instance
     */
    public UserEvent toUserEvent() {
        return UserEvent.builder()
                .userId(userId)
                .itemId(itemId)
                .eventType(eventType)
                .timestamp(Instant.now())
                .score(score)
                .sessionId(sessionId)
                .contextInfo(contextInfo)
                .build();
    }
}
