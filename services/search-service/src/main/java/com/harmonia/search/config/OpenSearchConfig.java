package com.harmonia.search.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.hc.core5.http.HttpHost;
import org.opensearch.client.json.jackson.JacksonJsonpMapper;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.OpenSearchTransport;
import org.opensearch.client.transport.httpclient5.ApacheHttpClient5TransportBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URISyntaxException;

@Configuration
@EnableConfigurationProperties(SearchProperties.class)
public class OpenSearchConfig {

    @Bean(destroyMethod = "close")
    OpenSearchTransport openSearchTransport(SearchProperties properties) {
        HttpHost host;
        try {
            host = HttpHost.create(properties.getUrl());
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException("Invalid OpenSearch URL: " + properties.getUrl(), ex);
        }
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return ApacheHttpClient5TransportBuilder.builder(host)
                .setMapper(new JacksonJsonpMapper(mapper))
                .build();
    }

    @Bean
    OpenSearchClient openSearchClient(OpenSearchTransport transport) {
        return new OpenSearchClient(transport);
    }
}
