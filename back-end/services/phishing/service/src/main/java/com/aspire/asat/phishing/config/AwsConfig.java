package com.aspire.asat.phishing.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
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
    public SqsClient sqsClient() {
        return SqsClient.builder()
                .region(Region.of(region))
                .credentialsProvider(getCredentialsProvider())
                .build();
    }

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(getCredentialsProvider())
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(getCredentialsProvider())
                .build();
    }

    @Bean
    public RekognitionClient rekognitionClient() {
        return RekognitionClient.builder()
                .region(Region.of(region))
                .credentialsProvider(getCredentialsProvider())
                .build();
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
