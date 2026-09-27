package com.aspire.asat.common.config.aws;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for AWS SSM Parameter Store integration
 */
@Data
@Component
@ConfigurationProperties(prefix = "aws.ssm")
public class AwsSsmConfig {
    
    /**
     * Enable/disable SSM parameter loading
     * Default: true
     */
    private boolean enabled = true;
    
    /**
     * Enable/disable KMS decryption
     * Default: true
     * Set to false for modules that don't need KMS decryption
     */
    private boolean kmsEnabled = true;
    
    /**
     * Parameter path prefix (e.g., /asat)
     * Default: /asat
     */
    private String parameterPathPrefix = "/asat";

    /**
     * Optional shared/common parameter path (e.g., /asat/common/dev/)
     * If not set, initializer derives it as {parameterPathPrefix}/common/{activeProfile}/
     */
    private String commonParameterPath;
    
    /**
     * KMS Key ID for decryption
     * Can be a key ID, key ARN, or alias (e.g., alias/asat-kms-key)
     */
    private String kmsKeyId;
    
    /**
     * AWS Region for SSM and KMS
     * If not set, will use aws.region from AwsRootCredentials
     */
    private String region;
    
    /**
     * Maximum number of parameters to fetch in a single request
     * Default: 10 (SSM limit is 10 per GetParameters call)
     */
    private int maxParametersPerRequest = 10;
    
    /**
     * Whether to fail on error or continue with fallback
     * Default: false (continue with fallback for local development)
     */
    private boolean failOnError = false;
}

