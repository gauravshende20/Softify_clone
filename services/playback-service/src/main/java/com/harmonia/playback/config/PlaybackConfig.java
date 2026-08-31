package com.harmonia.playback.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class PlaybackConfig {

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
    RestClient playlistRestClient(RestClient.Builder builder,
                                  @Value("${harmonia.playlist.base-url:http://localhost:8084}") String baseUrl) {
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
