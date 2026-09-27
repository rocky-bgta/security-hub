package com.aspire.sesame.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteLocatorConfiguration {

//    @Autowired
//    private final TokenService tokenService;

    @Value("${spring.cloud.gateway.routes.registration-uri}")
    private String registrationUri;

    @Value("${spring.cloud.gateway.routes.course-uri}")
    private String courseUri;

    @Bean
    public RouteLocator myRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(p -> p
                        .path("/registration/**")
                        .filters(f -> f
                                // .addRequestHeader("Authorization", "Bearer " /* + tokenService.getJwtToken()*/)
                                .rewritePath("/registration/(?<segment>.*)", "/${segment}")  // Added missing closing parenthesis
                        )
                        .uri(registrationUri))

                .route(p -> p
                        .path("/course/**")
                        .filters(f -> f
                                // .addRequestHeader("Authorization", "Bearer " /* + tokenService.getJwtToken()*/)
                                .rewritePath("/course/(?<segment>.*)", "/${segment}")  // Added missing closing parenthesis
                        )
                        .uri(courseUri))

                .build();
    }
}
