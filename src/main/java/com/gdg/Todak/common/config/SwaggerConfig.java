package com.gdg.Todak.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .components(new Components())
                .info(apiInfo());
    }

    private Info apiInfo() {
        return new Info()
                .title("2025 GDG 솔루션 챌린지 - 경북대 1팀 - Todak Service 명세서")
                .description("[Team Notion 바로가기](https://www.notion.so/2025-GDG-1-Todak-183260f9e1bd80438092de22a6dd801a)")
                .version("0.0.1");
    }
}
