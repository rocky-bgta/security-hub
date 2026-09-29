package com.aspire.asat.notification.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

/**
 * Condition that matches when AWS credentials are available from config or environment.
 * Used to create SqsClient/SesClient/S3Client only when credentials are present,
 * so the app can run without AWS (e.g. local dev) without SQS polling errors.
 */
public class AwsCredentialsAvailableCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        boolean sqsEnabled = context.getEnvironment().getProperty("aws.sqs.enabled", Boolean.class, true);
        if (!sqsEnabled) {
            return false;
        }

        String accessKey = context.getEnvironment().getProperty("aws.credentials.access-key");
        String secretKey = context.getEnvironment().getProperty("aws.credentials.secret-key");
        if (StringUtils.hasText(accessKey) && StringUtils.hasText(secretKey)) {
            return true;
        }
        if (StringUtils.hasText(System.getenv("AWS_ACCESS_KEY_ID")) && StringUtils.hasText(System.getenv("AWS_SECRET_ACCESS_KEY"))) {
            return true;
        }
        if (StringUtils.hasText(System.getenv("AWS_CREDENTIALS_ACCESS_KEY")) && StringUtils.hasText(System.getenv("AWS_CREDENTIALS_SECRET_KEY"))) {
            return true;
        }
        return false;
    }
}
