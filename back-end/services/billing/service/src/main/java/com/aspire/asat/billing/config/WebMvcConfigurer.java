package com.aspire.asat.billing.config;

import com.aspire.asat.billing.constant.WebApiUrlConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

/**
 * Web MVC configuration for the billing service.
 * Note: @EnableWebMvc is intentionally NOT used to preserve Spring Boot's auto-configuration
 * including Jackson serialization settings (e.g., Instant as ISO 8601 strings).
 */
@Configuration
public class WebMvcConfigurer implements org.springframework.web.servlet.config.annotation.WebMvcConfigurer {

    private final InternalServiceAuthInterceptor internalServiceAuthInterceptor;

    public WebMvcConfigurer(InternalServiceAuthInterceptor internalServiceAuthInterceptor) {
        this.internalServiceAuthInterceptor = internalServiceAuthInterceptor;
    }

    @Bean
    public org.springframework.web.servlet.config.annotation.WebMvcConfigurer corsConfigurer() {
        return new org.springframework.web.servlet.config.annotation.WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // CORS is handled by the gateway SecurityInterceptor
                // No CORS configuration needed here to prevent duplicate headers
            }
        };
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(internalServiceAuthInterceptor)
                .addPathPatterns(
                        WebApiUrlConstants.INVOICE_API + WebApiUrlConstants.CREATE,
                        WebApiUrlConstants.INVOICE_API + "/update/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/swagger-ui/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

}

