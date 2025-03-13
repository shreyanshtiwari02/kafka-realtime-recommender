#!/bin/bash

# Test script for the Kafka Realtime Recommender REST API
# This script tests the main endpoints of the API

BASE_URL="http://localhost:8080"
USER_ID="user123"
ITEM_ID="item456"

echo "===== Testing Kafka Realtime Recommender REST API ====="

# 1. Create an item
echo -e "\n1. Creating an item..."
curl -X POST "${BASE_URL}/api/items" \
  -H "Content-Type: application/json" \
  -d '{
    "id": "'${ITEM_ID}'",
    "name": "Test Item",
    "description": "This is a test item",
    "categories": ["electronics", "gadgets"],
    "features": {
      "price": 99.99,
      "rating": 4.5,
      "popularity": 0.85
    },
    "metadata": {
      "brand": "TestBrand",
      "color": "black"
    },
    "popularity": 0.85
  }'

# 2. Record a view event
echo -e "\n2. Recording a view event..."
curl -X POST "${BASE_URL}/api/events/view?userId=${USER_ID}&itemId=${ITEM_ID}"

# 3. Record a purchase event
echo -e "\n3. Recording a purchase event..."
curl -X POST "${BASE_URL}/api/events/purchase?userId=${USER_ID}&itemId=${ITEM_ID}"

# 4. Record a rating event
echo -e "\n4. Recording a rating event..."
curl -X POST "${BASE_URL}/api/events/rate?userId=${USER_ID}&itemId=${ITEM_ID}&score=4.5"

# 5. Get recommendations for a user
echo -e "\n5. Getting recommendations for user..."
curl -X GET "${BASE_URL}/api/recommendations?userId=${USER_ID}&limit=5"

echo -e "\n===== API Testing Complete ====="
