package com.github.xincao9.archetype.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI(@Value("${spring.application.name}") String applicationName) {
        return new OpenAPI()
                .info(new Info().title(applicationName).version("1.0.0").description("infra-framework project"));
    }
}