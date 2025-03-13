package com.recommender.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Generic JSON serializer/deserializer for Kafka.
 * Used to convert between Java objects and JSON for Kafka topics.
 * 
 * @param <T> The type of object to serialize/deserialize
 */
public class JsonSerde<T> implements Serde<T> {
    private static final Logger log = LoggerFactory.getLogger(JsonSerde.class);
    private final ObjectMapper mapper = new ObjectMapper();
    private final Class<T> type;

    public JsonSerde(Class<T> type) {
        this.type = type;
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // Nothing to configure
    }

    @Override
    public void close() {
        // Nothing to close
    }

    @Override
    public Serializer<T> serializer() {
        return new Serializer<T>() {
            @Override
            public void configure(Map<String, ?> configs, boolean isKey) {
                // Nothing to configure
            }

            @Override
            public byte[] serialize(String topic, T data) {
                if (data == null) {
                    return null;
                }
                
                try {
                    return mapper.writeValueAsBytes(data);
                } catch (Exception e) {
                    log.error("Error serializing JSON message", e);
                    return null;
                }
            }

            @Override
            public void close() {
                // Nothing to close
            }
        };
    }

    @Override
    public Deserializer<T> deserializer() {
        return new Deserializer<T>() {
            @Override
            public void configure(Map<String, ?> configs, boolean isKey) {
                // Nothing to configure
            }

            @Override
            public T deserialize(String topic, byte[] data) {
                if (data == null) {
                    return null;
                }
                
                try {
                    return mapper.readValue(data, type);
                } catch (Exception e) {
                    log.error("Error deserializing JSON message", e);
                    return null;
                }
            }

            @Override
            public void close() {
                // Nothing to close
            }
        };
    }
}
