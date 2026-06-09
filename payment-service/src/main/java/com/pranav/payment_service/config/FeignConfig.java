// FeignConfig.java
package com.pranav.payment_service.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Value("${internal.api-key}")
    private String internalApiKey;

    // Automatically adds X-Internal-Api-Key to every Feign request
    @Bean
    public RequestInterceptor internalApiKeyInterceptor() {
        return requestTemplate ->
                requestTemplate.header("X-Internal-Api-Key", internalApiKey);
    }
}