package com.fiap.techalert.config;

import java.time.Duration;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(TechAlertProperties.class)
public class RestClientConfig {

    @Bean
    RestClient geocodingRestClient(RestClient.Builder builder, TechAlertProperties properties) {
        return builder.clone()
                .baseUrl(properties.getOpenMeteo().getGeocodingUrl())
                .requestFactory(requestFactory())
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.USER_AGENT, "TechAlert/1.0")
                .build();
    }

    @Bean
    RestClient forecastRestClient(RestClient.Builder builder, TechAlertProperties properties) {
        return builder.clone()
                .baseUrl(properties.getOpenMeteo().getForecastUrl())
                .requestFactory(requestFactory())
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.USER_AGENT, "TechAlert/1.0")
                .build();
    }

    private SimpleClientHttpRequestFactory requestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(10));
        return factory;
    }
}
