package com.aspire.asat.common.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@AllArgsConstructor
public class S3Config {
    private final FileProps props;

    @Bean
    public S3Presigner s3Presigner() {
        var region = Region.of(props.getAws().getRegion());
        var builder = S3Presigner.builder().region(region);
        var accessKeyId = props.getAws().getAccessKeyId();
        var secretAccessKey = props.getAws().getSecretAccessKey();

        if (StringUtils.hasText(accessKeyId) && StringUtils.hasText(secretAccessKey)) {
            var credentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);
            return builder.credentialsProvider(StaticCredentialsProvider.create(credentials)).build();
        }

        return builder.credentialsProvider(DefaultCredentialsProvider.create()).build();
    }
}
