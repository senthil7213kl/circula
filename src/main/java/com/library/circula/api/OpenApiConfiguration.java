package com.library.circula.api;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {
    @Bean
    OpenAPI circulaOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Circula Library API")
                .version("1.0.0")
                .description("Manage library borrowers, physical book copies, and loans."));
    }
}
