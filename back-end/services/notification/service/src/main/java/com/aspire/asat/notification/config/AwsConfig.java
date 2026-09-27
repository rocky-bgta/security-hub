package com.aspire.asat.notification.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.util.Optional;

@Configuration
public class AwsConfig {

    @Value("${aws.region}")
    private String region;

    @Value("${aws.credentials.access-key:}")
    private String accessKey;

    @Value("${aws.credentials.secret-key:}")
    private String secretKey;

    @Bean
    @Conditional(AwsCredentialsAvailableCondition.class)
    public SqsClient sqsClient() {
        return SqsClient.builder()
                .region(getRegion())
                .credentialsProvider(getCredentialsProvider())
                .build();
    }

    @Bean
    public SesClient sesClient() {
        return SesClient.builder()
                .region(getRegion())
                .credentialsProvider(getCredentialsProvider())
                .build();
    }

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(getRegion())
                .credentialsProvider(getCredentialsProvider())
                .build();
    }

    private Region getRegion() {
        return Region.of(region);
    }

    private AwsCredentialsProvider getCredentialsProvider() {
        String resolvedAccessKey = StringUtils.hasText(accessKey) ? accessKey : Optional.ofNullable(System.getenv("AWS_CREDENTIALS_ACCESS_KEY")).orElse(System.getenv("AWS_ACCESS_KEY_ID"));
        String resolvedSecretKey = StringUtils.hasText(secretKey) ? secretKey : Optional.ofNullable(System.getenv("AWS_CREDENTIALS_SECRET_KEY")).orElse(System.getenv("AWS_SECRET_ACCESS_KEY"));
        if (StringUtils.hasText(resolvedAccessKey) && StringUtils.hasText(resolvedSecretKey)) {
            return StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(resolvedAccessKey, resolvedSecretKey));
        }
        return DefaultCredentialsProvider.create();
    }

}
