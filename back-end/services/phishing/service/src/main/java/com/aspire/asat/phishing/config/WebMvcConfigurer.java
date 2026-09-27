package com.aspire.asat.phishing.config;

import com.aspire.asat.phishing.constant.WebApiUrlConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

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
                // CORS is handled entirely by the Gateway SecurityInterceptor.
                // No CORS configuration here to prevent duplicate Access-Control-Allow-Origin headers.
            }
        };
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(internalServiceAuthInterceptor)
                .addPathPatterns(WebApiUrlConstants.INTERNAL_API_PATH + "/**");
    }
}
