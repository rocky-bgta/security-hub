package com.aspire.asat.vps.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {
        "com.aspire.asat.common"
})
public class CommonServiceConfig {
}
