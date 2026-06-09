package com.pranav.api_gateway.config;

import org.springdoc.core.properties.AbstractSwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.HashSet;
import java.util.Set;

@Configuration
public class SwaggerConfig {

    @Bean
    @Primary
    public SwaggerUiConfigProperties swaggerUiConfigProperties() {
        SwaggerUiConfigProperties config =
                new SwaggerUiConfigProperties();

        // Each microservice listed here
        Set<AbstractSwaggerUiConfigProperties.SwaggerUrl> urls =
                new HashSet<>();

        urls.add(swaggerUrl("Auth Service",
                "/auth-service/v3/api-docs"));
        urls.add(swaggerUrl("Merchant Service",
                "/merchant-service/v3/api-docs"));
        urls.add(swaggerUrl("Payment Service",
                "/payment-service/v3/api-docs"));
        urls.add(swaggerUrl("Wallet Service",
                "/wallet-service/v3/api-docs"));
        urls.add(swaggerUrl("Notification Service",
                "/notification-service/v3/api-docs"));

        config.setUrls(urls);
        return config;
    }

    private AbstractSwaggerUiConfigProperties.SwaggerUrl swaggerUrl(
            String name, String url) {
        AbstractSwaggerUiConfigProperties.SwaggerUrl swaggerUrl =
                new AbstractSwaggerUiConfigProperties.SwaggerUrl();
        swaggerUrl.setName(name);
        swaggerUrl.setUrl(url);
        return swaggerUrl;
    }
}