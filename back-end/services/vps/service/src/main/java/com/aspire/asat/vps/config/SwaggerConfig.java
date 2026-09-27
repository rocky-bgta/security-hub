package com.aspire.asat.vps.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("VPS Service API")
                        .version("1.0")
                        .description("Video Processing and Storage service API with JWT authentication"))
                .servers(List.of(
                        new Server().url("https://dev.aspireelearning.com/gateway/vps").description("Gateway Server (Default)"),
                        new Server().url("http://localhost:7030/vps").description("Gateway Server (Default)"),
                        new Server().url("http://localhost:8086/vps").description("Direct VPS Service")
                ))
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
                                .description("Base64 encoded user context information. Required when calling APIs directly (not through gateway). Example: eyJ1c2VySWQiOiJzdXBlci1hZG1pbiIsInVzZXJuYW1lIjoic3VwZXItYWRtaW4iLCJ1c2VyVHlwZSI6IlNVUEVSX0FETUlOIn0=")));
    }
}
