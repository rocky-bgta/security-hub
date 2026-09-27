package com.aspire.asat.cms.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.Optional;

@Configuration
public class AwsConfig {

    @Value("${aws.region:us-east-1}")
    private String region;

    @Value("${aws.credentials.access-key:${aws.accessKeyId:}}")
    private String accessKey;

    @Value("${aws.credentials.secret-key:${aws.secretAccessKey:}}")
    private String secretKey;

    @Bean
    public S3Client s3Client() {
        String resolvedAccessKey = resolveAccessKey();
        String resolvedSecretKey = resolveSecretKey();

        // If credentials are provided, use them; otherwise use default credential provider chain
        if (StringUtils.hasText(resolvedAccessKey) && StringUtils.hasText(resolvedSecretKey)) {
            return S3Client.builder()
                    .region(Region.of(region))
                    .credentialsProvider(getCredentialsProvider(resolvedAccessKey, resolvedSecretKey))
                    .build();
        }
        
        // If credentials are not provided, AWS SDK will use default credential provider chain
        // (environment variables, system properties, IAM role, etc.)
        return S3Client.builder()
                .region(Region.of(region))
                .build();
    }

    private StaticCredentialsProvider getCredentialsProvider(String resolvedAccessKey, String resolvedSecretKey) {
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(resolvedAccessKey, resolvedSecretKey));
    }

    private String resolveAccessKey() {
        if (StringUtils.hasText(accessKey)) {
            return accessKey;
        }
        return Optional.ofNullable(System.getenv("AWS_CREDENTIALS_ACCESS_KEY"))
                .orElse(System.getenv("AWS_ACCESS_KEY_ID"));
    }

    private String resolveSecretKey() {
        if (StringUtils.hasText(secretKey)) {
            return secretKey;
        }
        return Optional.ofNullable(System.getenv("AWS_CREDENTIALS_SECRET_KEY"))
                .orElse(System.getenv("AWS_SECRET_ACCESS_KEY"));
    }
}

