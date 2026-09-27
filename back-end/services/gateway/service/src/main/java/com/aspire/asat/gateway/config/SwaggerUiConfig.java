package com.aspire.asat.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;
import org.springframework.http.MediaType;

@Configuration
public class SwaggerUiConfig {

    @Value("${GATEWAY.BASE_URL:http://localhost:7030/gateway}")
    private String gatewayBaseUrl;

    @Bean
    public RouterFunction<ServerResponse> swaggerUiRouterFunction() {
        return RouterFunctions.route(GET("/swagger-ui/swagger-config"), request -> {
            // Get the base URL for OAuth redirect
            // If accessed via nginx (/gateway prefix), we need to account for that
            String oauthRedirectBase = gatewayBaseUrl.replaceAll("/gateway$", "");
            
            // Determine if we're behind nginx (URL ends with /gateway)
            boolean behindNginx = gatewayBaseUrl.endsWith("/gateway");
            
            // For API doc URLs, use relative paths that work with nginx
            // When behind nginx: /gateway/v3/api-docs for gateway, /gateway/auth/v3/api-docs for auth
            // When direct: /v3/api-docs for gateway, paths go through gateway routing
            String gatewayApiDocPath = behindNginx ? "/gateway/v3/api-docs" : "/v3/api-docs";
            
            final String config = String.format("""
                {
                    "urls": [
                        {
                            "url": "%s",
                            "name": "Gateway API"
                        },
                        {
                            "url": "/gateway/auth/v3/api-docs",
                            "name": "Auth Service"
                        },
                        {
                            "url": "/gateway/registration/v3/api-docs",
                            "name": "Registration Service"
                        },
                        {
                            "url": "/gateway/cms/v3/api-docs",
                            "name": "CMS Service"
                        },
                        {
                            "url": "/gateway/billing/v3/api-docs",
                            "name": "Billing Service"
                        },
                        {
                            "url": "/gateway/notification/v3/api-docs",
                            "name": "Notification Service"
                        },
                        {
                            "url": "/gateway/universal/v3/api-docs",
                            "name": "Universal Service"
                        },
                        {
                            "url": "/gateway/breach/v3/api-docs",
                            "name": "Breach Detection Service"
                        }
                    ],
                    "validatorUrl": null,
                    "oauth2RedirectUrl": "%s/gateway/webjars/springdoc-openapi-ui/oauth2-redirect.html"
                }
                """, gatewayApiDocPath, oauthRedirectBase);
            return ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(config);
        });
    }
}
