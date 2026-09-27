package com.aspire.asat.common.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "aws")
public class AwsRootCredentials {
    private String region;
    private String accessKeyId;
    private String secretAccessKey;
}
