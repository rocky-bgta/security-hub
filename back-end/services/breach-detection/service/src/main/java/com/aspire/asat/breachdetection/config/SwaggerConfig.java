package com.aspire.asat.breachdetection.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${breach.swagger.gateway-url:https://dev.aspireelearning.com/gateway/breach}")
    private String gatewayServerUrl;

    @Value("${breach.swagger.direct-url:http://localhost:6080/breach}")
    private String directServerUrl;

    @Bean
    public OpenAPI customOpenAPI() {
        List<Server> servers = new ArrayList<>();
        servers.add(new Server().url(trimTrailingSlash(gatewayServerUrl)).description("API Gateway (profile-specific)"));
        servers.add(new Server().url(trimTrailingSlash(directServerUrl)).description("Direct breach service (context-path /breach)"));

        return new OpenAPI()
                .info(new Info()
                        .title("Breach Service API")
                        .version("1.0")
                        .description("Breach detection configuration, InsecureWeb sync, and Shodan monitoring API"))
                .servers(servers)
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth").addList("currentContextAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT Authorization header using the Bearer scheme. Example: \"Authorization: Bearer {token}\""))
                        .addSecuritySchemes("currentContextAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("CurrentContext")
                                .description("Base64 encoded user context information. Required when calling APIs directly (not through gateway).")));
    }

    private static String trimTrailingSlash(String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }
        String t = url.trim();
        while (t.endsWith("/")) {
            t = t.substring(0, t.length() - 1);
        }
        return t;
    }
}
