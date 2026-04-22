package com.apiCodeGenerator.common.apputil.utils.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
//        	.addServersItem(new Server().url("https://qa.topiatech.co.uk/demo"))
                .info(new Info()
                        .title("Market-Access Service API")
                        .version("1.0")
                        .description("RestAPIs documentation for market-access Project")
                        .contact(new Contact().name("Support Team").email("support@example.com"))
                ).addSecurityItem(new SecurityRequirement().addList("bearerAuth"))

                // Add Security Scheme
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .name("Authorization")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                );
    }

    @Bean
    public OpenApiCustomizer sortOperationsCustomizer() {
        return openApi -> {
            if (openApi.getPaths() == null) return;

            openApi.getPaths().forEach((path, pathItem) -> {
                Map<PathItem.HttpMethod, Operation> operations = pathItem.readOperationsMap();

                Map<PathItem.HttpMethod, Operation> sorted = new LinkedHashMap<>();
                operations.entrySet().stream()
                        .sorted((e1, e2) ->
                                Integer.compare(getOrder(e1.getKey()), getOrder(e2.getKey())))
                        .forEachOrdered(e -> sorted.put(e.getKey(), e.getValue()));

                PathItem newPathItem = new PathItem();
                sorted.forEach(newPathItem::operation);

                openApi.getPaths().addPathItem(path, newPathItem);
            });
        };
    }

    private int getOrder(PathItem.HttpMethod method) {
        return switch (method) {
            case GET -> 1;
            case POST -> 2;
            case PUT -> 3;
            case DELETE -> 4;
            default -> 99;
        };
    }
}






