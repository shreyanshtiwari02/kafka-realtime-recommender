package com.recommender.controller;

import com.recommender.dto.UserEventRequest;
import com.recommender.model.UserEvent;
import com.recommender.service.UserEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * REST controller for user events.
 * Provides endpoints for recording user interactions.
 */
@RestController
@RequestMapping("/api/events")
@Slf4j
@RequiredArgsConstructor
public class UserEventController {
    
    private final UserEventService userEventService;
    
    /**
     * Records a user event.
     * 
     * @param request The user event request
     * @return The recorded user event
     */
    @PostMapping
    public ResponseEntity<UserEvent> recordEvent(@Valid @RequestBody UserEventRequest request) {
        log.info("Received user event request: {}", request);
        
        UserEvent userEvent = request.toUserEvent();
        UserEvent savedEvent = userEventService.publishUserEvent(userEvent);
        
        return new ResponseEntity<>(savedEvent, HttpStatus.CREATED);
    }
    
    /**
     * Records a view event.
     * Convenience endpoint for recording view events.
     * 
     * @param userId The user ID
     * @param itemId The item ID
     * @return The recorded user event
     */
    @PostMapping("/view")
    public ResponseEntity<UserEvent> recordViewEvent(
            @RequestParam String userId,
            @RequestParam String itemId,
            @RequestParam(required = false) String sessionId) {
        
        UserEventRequest request = UserEventRequest.builder()
                .userId(userId)
                .itemId(itemId)
                .eventType(UserEvent.EventType.VIEW)
                .sessionId(sessionId)
                .build();
        
        UserEvent userEvent = request.toUserEvent();
        UserEvent savedEvent = userEventService.publishUserEvent(userEvent);
        
        return new ResponseEntity<>(savedEvent, HttpStatus.CREATED);
    }
    
    /**
     * Records a purchase event.
     * Convenience endpoint for recording purchase events.
     * 
     * @param userId The user ID
     * @param itemId The item ID
     * @return The recorded user event
     */
    @PostMapping("/purchase")
    public ResponseEntity<UserEvent> recordPurchaseEvent(
            @RequestParam String userId,
            @RequestParam String itemId,
            @RequestParam(required = false) String sessionId) {
        
        UserEventRequest request = UserEventRequest.builder()
                .userId(userId)
                .itemId(itemId)
                .eventType(UserEvent.EventType.PURCHASE)
                .sessionId(sessionId)
                .build();
        
        UserEvent userEvent = request.toUserEvent();
        UserEvent savedEvent = userEventService.publishUserEvent(userEvent);
        
        return new ResponseEntity<>(savedEvent, HttpStatus.CREATED);
    }
    
    /**
     * Records a rating event.
     * Convenience endpoint for recording rating events.
     * 
     * @param userId The user ID
     * @param itemId The item ID
     * @param score The rating score
     * @return The recorded user event
     */
    @PostMapping("/rate")
    public ResponseEntity<UserEvent> recordRatingEvent(
            @RequestParam String userId,
            @RequestParam String itemId,
            @RequestParam Double score,
            @RequestParam(required = false) String sessionId) {
        
        UserEventRequest request = UserEventRequest.builder()
                .userId(userId)
                .itemId(itemId)
                .eventType(UserEvent.EventType.RATE)
                .score(score)
                .sessionId(sessionId)
                .build();
        
        UserEvent userEvent = request.toUserEvent();
        UserEvent savedEvent = userEventService.publishUserEvent(userEvent);
        
        return new ResponseEntity<>(savedEvent, HttpStatus.CREATED);
    }
}
