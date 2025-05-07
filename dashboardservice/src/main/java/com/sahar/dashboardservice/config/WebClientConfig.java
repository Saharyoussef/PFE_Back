package com.sahar.dashboardservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Value("${python-api.screenshot-service.base-url}") // Inject value from application.yml
    private String screenshotServiceBaseUrl;

    @Bean
    public WebClient screenshotWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(screenshotServiceBaseUrl)
                // Add default headers if needed (e.g., Content-Type)
                // .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
