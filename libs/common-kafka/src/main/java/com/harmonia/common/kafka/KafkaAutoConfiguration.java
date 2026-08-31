package com.harmonia.common.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@AutoConfiguration
@AutoConfigureBefore(org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration.class)
@ConditionalOnClass(KafkaTemplate.class)
public class KafkaAutoConfiguration {

    @Bean
    public ProducerFactory<String, DomainEvent> domainEventProducerFactory(KafkaProperties properties) {
        Map<String, Object> config = new HashMap<>(properties.buildProducerProperties(null));
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        config.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, DomainEvent> domainEventKafkaTemplate(
            ProducerFactory<String, DomainEvent> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public ConsumerFactory<String, DomainEvent> domainEventConsumerFactory(KafkaProperties properties) {
        Map<String, Object> config = new HashMap<>(properties.buildConsumerProperties(null));
        config.remove(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG);
        config.remove(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG);
        config.remove(JsonDeserializer.VALUE_DEFAULT_TYPE);
        config.remove(JsonDeserializer.KEY_DEFAULT_TYPE);
        config.remove(JsonDeserializer.TRUSTED_PACKAGES);
        config.remove(JsonDeserializer.USE_TYPE_INFO_HEADERS);
        config.remove(JsonDeserializer.TYPE_MAPPINGS);
        JsonDeserializer<DomainEvent> deserializer = new JsonDeserializer<>(DomainEvent.class);
        deserializer.setUseTypeHeaders(false);
        deserializer.addTrustedPackages("com.harmonia.common.kafka");
        return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DomainEvent> kafkaListenerContainerFactory(
            ConsumerFactory<String, DomainEvent> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, DomainEvent> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

    @Bean
    public DomainEventPublisher domainEventPublisher(KafkaTemplate<String, DomainEvent> template) {
        return new DomainEventPublisher(template);
    }

    @Bean
    public NewTopic userEventsTopic() {
        return TopicBuilder.name(Topics.USER).partitions(6).replicas(1).build();
    }

    @Bean
    public NewTopic catalogEventsTopic() {
        return TopicBuilder.name(Topics.CATALOG).partitions(6).replicas(1).build();
    }

    @Bean
    public NewTopic playlistEventsTopic() {
        return TopicBuilder.name(Topics.PLAYLIST).partitions(6).replicas(1).build();
    }

    @Bean
    public NewTopic playbackEventsTopic() {
        return TopicBuilder.name(Topics.PLAYBACK).partitions(12).replicas(1).build();
    }

    @Bean
    public NewTopic socialEventsTopic() {
        return TopicBuilder.name(Topics.SOCIAL).partitions(6).replicas(1).build();
    }

    @Bean
    public NewTopic searchEventsTopic() {
        return TopicBuilder.name(Topics.SEARCH).partitions(6).replicas(1).build();
    }
}
