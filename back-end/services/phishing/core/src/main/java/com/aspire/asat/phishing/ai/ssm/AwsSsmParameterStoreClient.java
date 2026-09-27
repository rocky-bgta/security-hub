package com.aspire.asat.phishing.ai.ssm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;
import software.amazon.awssdk.services.ssm.model.ParameterNotFoundException;
import software.amazon.awssdk.services.ssm.model.PutParameterRequest;
import software.amazon.awssdk.services.ssm.model.SsmException;

/**
 * Minimal AWS SSM client wrapper for SecureString parameters.
 */
@Component
@Slf4j
public class AwsSsmParameterStoreClient {

    private final SsmClient ssmClient;

    public AwsSsmParameterStoreClient(
            @Value("${aws.region:us-east-1}") String region,
            @Value("${aws.credentials.access-key:}") String accessKeyId,
            @Value("${aws.credentials.secret-key:}") String secretAccessKey,
            @Value("${aws.access-key-id:}") String accessKeyIdAlt,
            @Value("${aws.secret-access-key:}") String secretAccessKeyAlt
    ) {
        String resolvedAccessKeyId = StringUtils.hasText(accessKeyId) ? accessKeyId : accessKeyIdAlt;
        String resolvedSecretAccessKey = StringUtils.hasText(secretAccessKey) ? secretAccessKey : secretAccessKeyAlt;

        AwsCredentialsProvider credentialsProvider;
        if (StringUtils.hasText(resolvedAccessKeyId) && StringUtils.hasText(resolvedSecretAccessKey)) {
            credentialsProvider = StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(resolvedAccessKeyId, resolvedSecretAccessKey)
            );
        } else {
            credentialsProvider = DefaultCredentialsProvider.create();
        }

        this.ssmClient = SsmClient.builder()
                .region(Region.of(region))
                .credentialsProvider(credentialsProvider)
                .build();
    }

    public void putStringParameter(String name, String value, boolean secureString, boolean overwrite) {
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("SSM parameter name is required");
        }
        if (value == null) {
            throw new IllegalArgumentException("SSM parameter value is required");
        }

        String type = secureString ? "SecureString" : "String";
        PutParameterRequest request = PutParameterRequest.builder()
                .name(name)
                .value(value)
                .type(type)
                .overwrite(overwrite)
                .build();

        try {
            // Do not log the value.
            log.debug("Putting SSM parameter (type={}, overwrite={}): {}", type, overwrite, name);
            ssmClient.putParameter(request);
        } catch (SsmException e) {
            log.error("Failed to put SSM parameter: {}", name, e);
            throw e;
        }
    }

    public String getParameter(String name) {
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("SSM parameter name is required");
        }

        try {
            GetParameterRequest request = GetParameterRequest.builder()
                    .name(name)
                    .withDecryption(true)
                    .build();
            GetParameterResponse response = ssmClient.getParameter(request);
            return response.parameter().value();
        } catch (ParameterNotFoundException e) {
            return null;
        }
    }
}

