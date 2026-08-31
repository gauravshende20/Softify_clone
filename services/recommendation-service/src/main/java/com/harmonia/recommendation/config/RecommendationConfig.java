package com.harmonia.recommendation.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class RecommendationConfig {

    @Bean
    RestClient catalogRestClient(RestClient.Builder builder,
                                 @Value("${harmonia.catalog.base-url:http://localhost:8083}") String baseUrl) {
        return builder.clone()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory())
                .requestInterceptor(new DownstreamAuthInterceptor())
                .build();
    }

    @Bean
    RestClient userRestClient(RestClient.Builder builder,
                              @Value("${harmonia.user.base-url:http://localhost:8082}") String baseUrl) {
        return builder.clone()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory())
                .requestInterceptor(new DownstreamAuthInterceptor())
                .build();
    }

    private static JdkClientHttpRequestFactory requestFactory() {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(3));
        return factory;
    }
}
