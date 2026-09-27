package com.aspire.asat.universal.config;

import com.aspire.asat.universal.service.impl.YamlMessageSourceImpl;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InternationalizationConfig implements WebMvcConfigurer {

    @Bean
    public MessageSource messageSource() {
        return new YamlMessageSourceImpl();
    }

}