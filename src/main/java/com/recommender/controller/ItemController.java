package com.recommender.controller;

import com.recommender.dto.ItemRequest;
import com.recommender.model.Item;
import com.recommender.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * REST controller for items.
 * Provides endpoints for managing items in the recommendation system.
 */
@RestController
@RequestMapping("/api/items")
@Slf4j
@RequiredArgsConstructor
public class ItemController {
    
    private final ItemService itemService;
    
    /**
     * Creates a new item.
     * 
     * @param request The item request
     * @return The created item
     */
    @PostMapping
    public ResponseEntity<Item> createItem(@Valid @RequestBody ItemRequest request) {
        log.info("Received create item request: {}", request);
        
        Item item = request.toItem();
        Item savedItem = itemService.publishItem(item);
        
        return new ResponseEntity<>(savedItem, HttpStatus.CREATED);
    }
    
    /**
     * Updates an existing item.
     * 
     * @param id The item ID
     * @param request The item request
     * @return The updated item
     */
    @PutMapping("/{id}")
    public ResponseEntity<Item> updateItem(
            @PathVariable String id,
            @Valid @RequestBody ItemRequest request) {
        
        log.info("Received update item request for item {}: {}", id, request);
        
        // Ensure the ID in the path matches the ID in the request
        if (!id.equals(request.getId())) {
            log.warn("Item ID in path ({}) does not match ID in request ({})", id, request.getId());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        
        Item item = request.toItem();
        Item updatedItem = itemService.updateItem(item);
        
        return new ResponseEntity<>(updatedItem, HttpStatus.OK);
    }
    
    /**
     * Updates the popularity of an item.
     * 
     * @param id The item ID
     * @param popularity The new popularity value
     * @return The updated item
     */
    @PatchMapping("/{id}/popularity")
    public ResponseEntity<Item> updateItemPopularity(
            @PathVariable String id,
            @RequestParam double popularity) {
        
        log.info("Received update item popularity request for item {}: {}", id, popularity);
        
        // Create a minimal item with just the ID and popularity
        Item item = Item.builder()
                .id(id)
                .popularity(popularity)
                .lastUpdateTimestamp(System.currentTimeMillis())
                .build();
        
        Item updatedItem = itemService.updateItem(item);
        
        return new ResponseEntity<>(updatedItem, HttpStatus.OK);
    }
}
