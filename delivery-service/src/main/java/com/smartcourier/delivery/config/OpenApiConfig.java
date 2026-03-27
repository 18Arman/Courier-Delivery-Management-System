package com.smartcourier.delivery.config;

import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenApiCustomizer deliveryOpenApiCustomizer() {
        return openApi -> openApi.setServers(List.of(new Server().url("/").description("Current host")));
    }
}
