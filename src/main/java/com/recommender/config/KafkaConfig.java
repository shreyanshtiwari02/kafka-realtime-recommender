package com.recommender.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka configuration for the recommendation system.
 * Defines Kafka topics, Kafka Streams configuration, and other Kafka-related beans.
 */
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.streams.application-id}")
    private String applicationId;

    // Topic names
    public static final String USER_EVENTS_TOPIC = "user-events";
    public static final String USER_PROFILES_TOPIC = "user-profiles";
    public static final String RECOMMENDATIONS_TOPIC = "recommendations";
    public static final String ITEMS_TOPIC = "items";
    public static final String MODEL_UPDATES_TOPIC = "model-updates";
    public static final String AB_TEST_RESULTS_TOPIC = "ab-test-results";

    /**
     * Kafka Streams configuration.
     */
    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public KafkaStreamsConfiguration kStreamsConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, applicationId);
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        // Enable record cache for better performance
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 10 * 1024 * 1024L); // 10MB
        // Set commit interval
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1000);
        // Configure processing guarantee
        props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE_V2);
        return new KafkaStreamsConfiguration(props);
    }

    /**
     * Kafka Admin client for managing topics.
     */
    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }

    /**
     * Create the user events topic.
     */
    @Bean
    public NewTopic userEventsTopic() {
        return TopicBuilder.name(USER_EVENTS_TOPIC)
                .partitions(8)
                .replicas(1)
                .compact()
                .build();
    }

    /**
     * Create the user profiles topic.
     */
    @Bean
    public NewTopic userProfilesTopic() {
        return TopicBuilder.name(USER_PROFILES_TOPIC)
                .partitions(8)
                .replicas(1)
                .compact()
                .build();
    }

    /**
     * Create the recommendations topic.
     */
    @Bean
    public NewTopic recommendationsTopic() {
        return TopicBuilder.name(RECOMMENDATIONS_TOPIC)
                .partitions(8)
                .replicas(1)
                .build();
    }

    /**
     * Create the items topic.
     */
    @Bean
    public NewTopic itemsTopic() {
        return TopicBuilder.name(ITEMS_TOPIC)
                .partitions(4)
                .replicas(1)
                .compact()
                .build();
    }

    /**
     * Create the model updates topic.
     */
    @Bean
    public NewTopic modelUpdatesTopic() {
        return TopicBuilder.name(MODEL_UPDATES_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }

    /**
     * Create the A/B test results topic.
     */
    @Bean
    public NewTopic abTestResultsTopic() {
        return TopicBuilder.name(AB_TEST_RESULTS_TOPIC)
                .partitions(4)
                .replicas(1)
                .build();
    }
}
