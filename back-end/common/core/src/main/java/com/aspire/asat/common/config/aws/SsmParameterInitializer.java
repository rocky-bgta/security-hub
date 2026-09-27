package com.aspire.asat.common.config.aws;

import com.aspire.asat.common.config.AwsRootCredentials;
import com.aspire.asat.common.service.aws.KmsService;
import com.aspire.asat.common.service.aws.SsmService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * ApplicationContextInitializer that loads parameters from AWS SSM Parameter Store
 * and optionally decrypts them using AWS KMS before adding them to Spring Environment
 */
@Slf4j
public class SsmParameterInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    
    private static final String PROPERTY_SOURCE_NAME = "ssmParameters";
    
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        
        // Get configuration
        AwsSsmConfig ssmConfig = getSsmConfig(environment);
        
        // Check if SSM is enabled
        if (!ssmConfig.isEnabled()) {
            log.info("SSM parameter loading is disabled");
            return;
        }
        
        // Get active profile
        String[] activeProfiles = environment.getActiveProfiles();
        String activeProfile = activeProfiles.length > 0 ? activeProfiles[0] : "local";
        
        // Get service name from application context
        String serviceName = getServiceName(applicationContext, environment);
        
        // Build parameter path
        String parameterPath = buildParameterPath(ssmConfig.getParameterPathPrefix(), activeProfile, serviceName);
        String commonParameterPath = resolveCommonParameterPath(ssmConfig, activeProfile);
        
        log.info("Initializing SSM parameters for service: {}, profile: {}, commonPath: {}, servicePath: {}",
                serviceName, activeProfile, commonParameterPath, parameterPath);
        
        // Get AWS region
        String region = ssmConfig.getRegion();
        if (region == null || region.isEmpty()) {
            AwsRootCredentials awsConfig = getAwsConfig(environment);
            region = awsConfig != null && awsConfig.getRegion() != null 
                    ? awsConfig.getRegion() 
                    : "us-east-1";
        }
        
        SsmService ssmService = null;
        KmsService kmsService = null;
        
        try {
            AwsRootCredentials awsConfig = getAwsConfig(environment);
            String accessKeyId = resolveAccessKeyId(environment, awsConfig);
            String secretAccessKey = resolveSecretAccessKey(environment, awsConfig);

            // Initialize SSM service
            ssmService = new SsmService(region, accessKeyId, secretAccessKey);
            
            // Check if SSM is available
            if (!ssmService.isAvailable()) {
                if (ssmConfig.isFailOnError()) {
                    throw new RuntimeException("SSM is not available and failOnError is true");
                }
                return;
            }
            
            // Fetch shared/common parameters first, then overlay service-specific parameters
            Map<String, String> parameters = new HashMap<>();
            Map<String, String> commonParameters = ssmService.fetchParametersByPath(commonParameterPath);
            if (!commonParameters.isEmpty()) {
                parameters.putAll(commonParameters);
                log.info("Loaded {} shared parameters from common path: {}", commonParameters.size(), commonParameterPath);
            } else {
                log.warn("No parameters found at common path: {}", commonParameterPath);
            }

            Map<String, String> serviceParameters = ssmService.fetchParametersByPath(parameterPath);
            if (!serviceParameters.isEmpty()) {
                // service-specific parameters should override shared/common parameters on key collision
                parameters.putAll(serviceParameters);
                log.info("Loaded {} service parameters from service path: {}", serviceParameters.size(), parameterPath);
            } else {
                log.warn("No parameters found at service path: {}", parameterPath);
            }

            if (parameters.isEmpty()) {
                log.warn("No parameters found at common or service path. Using fallback configuration.");
                return;
            }
            
            // Initialize KMS service if enabled
            if (ssmConfig.isKmsEnabled()) {
                kmsService = new KmsService(region, ssmConfig.getKmsKeyId(), true);
                
                // Decrypt parameters if KMS is enabled
                if (kmsService.isAvailable()) {
                    log.info("Decrypting {} parameters using KMS", parameters.size());
                    parameters = kmsService.decryptAll(parameters);
                } else {
                    log.warn("KMS is not available, parameters will not be decrypted");
                }
            } else {
                log.info("KMS decryption is disabled for this module");
            }
            
            // Skip null/blank values so YAML and env defaults are not overwritten by empty SSM params
            Map<String, String> filtered = new HashMap<>();
            for (Map.Entry<String, String> e : parameters.entrySet()) {
                if (e.getValue() != null && !e.getValue().isBlank()) {
                    filtered.put(e.getKey(), e.getValue());
                } else {
                    log.debug("Skipping SSM parameter with empty value: {}", e.getKey());
                }
            }
            parameters = filtered;
            if (parameters.isEmpty()) {
                log.warn("All SSM parameters had empty values. Using fallback configuration.");
                return;
            }
            
            // Add parameters to Spring Environment (MapPropertySource expects Map<String, Object>)
            Map<String, Object> source = new HashMap<>(parameters);
            MapPropertySource propertySource = new MapPropertySource(PROPERTY_SOURCE_NAME, source);
            environment.getPropertySources().addFirst(propertySource);
            
            log.info("Successfully loaded {} parameters from SSM into Spring Environment", parameters.size());
            
        } catch (Exception e) {
            if (ssmConfig.isFailOnError()) {
                log.error("Failed to load SSM parameters and failOnError is true", e);
                throw new RuntimeException("Failed to initialize SSM parameters: " + e.getMessage(), e);
            } else {
                log.warn("Failed to load SSM parameters, continuing with fallback configuration: {}", e.getMessage());
            }
        } finally {
            // Clean up resources
            if (ssmService != null) {
                ssmService.close();
            }
            if (kmsService != null) {
                kmsService.close();
            }
        }
    }
    
    /**
     * Get SSM configuration from environment
     */
    private AwsSsmConfig getSsmConfig(ConfigurableEnvironment environment) {
        AwsSsmConfig config = new AwsSsmConfig();
        
        // Load from environment properties
        config.setEnabled(environment.getProperty("aws.ssm.enabled", Boolean.class, true));
        config.setKmsEnabled(environment.getProperty("aws.ssm.kms-enabled", Boolean.class, true));
        config.setParameterPathPrefix(environment.getProperty("aws.ssm.parameter-path-prefix", "/asat"));
        config.setCommonParameterPath(environment.getProperty("aws.ssm.common-parameter-path", String.class));
        config.setKmsKeyId(environment.getProperty("aws.ssm.kms-key-id", String.class));
        config.setRegion(environment.getProperty("aws.ssm.region", String.class));
        config.setFailOnError(environment.getProperty("aws.ssm.fail-on-error", Boolean.class, false));
        
        return config;
    }
    
    /**
     * Get AWS root configuration from environment
     */
    private AwsRootCredentials getAwsConfig(ConfigurableEnvironment environment) {
        AwsRootCredentials config = new AwsRootCredentials();
        config.setRegion(environment.getProperty("aws.region", String.class));
        config.setAccessKeyId(environment.getProperty("aws.access-key-id", String.class));
        config.setSecretAccessKey(environment.getProperty("aws.secret-access-key", String.class));
        return config;
    }

    private String resolveAccessKeyId(ConfigurableEnvironment environment, AwsRootCredentials awsConfig) {
        String fromSsmStyleConfig = environment.getProperty("aws.credentials.access-key", String.class);
        if (StringUtils.hasText(fromSsmStyleConfig)) {
            return fromSsmStyleConfig;
        }
        if (awsConfig != null && StringUtils.hasText(awsConfig.getAccessKeyId())) {
            return awsConfig.getAccessKeyId();
        }
        return Optional.ofNullable(System.getenv("AWS_ACCESS_KEY_ID"))
                .orElse(System.getenv("AWS_CREDENTIALS_ACCESS_KEY"));
    }

    private String resolveSecretAccessKey(ConfigurableEnvironment environment, AwsRootCredentials awsConfig) {
        String fromSsmStyleConfig = environment.getProperty("aws.credentials.secret-key", String.class);
        if (StringUtils.hasText(fromSsmStyleConfig)) {
            return fromSsmStyleConfig;
        }
        if (awsConfig != null && StringUtils.hasText(awsConfig.getSecretAccessKey())) {
            return awsConfig.getSecretAccessKey();
        }
        return Optional.ofNullable(System.getenv("AWS_SECRET_ACCESS_KEY"))
                .orElse(System.getenv("AWS_CREDENTIALS_SECRET_KEY"));
    }
    
    /**
     * Get service name from application context or environment
     */
    private String getServiceName(ConfigurableApplicationContext applicationContext, ConfigurableEnvironment environment) {
        // Try to get from environment property first
        String serviceName = environment.getProperty("spring.application.name");
        
        if (serviceName != null && !serviceName.isEmpty()) {
            // Remove common suffixes
            serviceName = serviceName.replace("-service", "");
            return serviceName + "-service";
        }
        
        // Try to infer from application class name
        String applicationClassName = applicationContext.getApplicationName();
        if (applicationClassName != null) {
            // Extract service name from class name (e.g., AuthApplication -> auth-service)
            String className = applicationClassName.contains(".") 
                    ? applicationClassName.substring(applicationClassName.lastIndexOf('.') + 1)
                    : applicationClassName;
            
            if (className.endsWith("Application")) {
                String baseName = className.substring(0, className.length() - "Application".length());
                return baseName.toLowerCase() + "-service";
            }
        }
        
        // Default fallback
        return "unknown-service";
    }
    
    /**
     * Build parameter path from components
     */
    private String buildParameterPath(String prefix, String profile, String serviceName) {
        // Ensure prefix doesn't end with /
        String cleanPrefix = prefix.endsWith("/") ? prefix.substring(0, prefix.length() - 1) : prefix;
        
        // Ensure service name doesn't have leading/trailing slashes
        String cleanServiceName = serviceName.startsWith("/") ? serviceName.substring(1) : serviceName;
        cleanServiceName = cleanServiceName.endsWith("/") ? cleanServiceName.substring(0, cleanServiceName.length() - 1) : cleanServiceName;
        
        return String.format("%s/%s/%s/", cleanPrefix, profile, cleanServiceName);
    }

    /**
     * Resolve shared/common parameter path.
     * Uses explicit config if provided, otherwise defaults to {prefix}/common/{profile}/
     */
    private String resolveCommonParameterPath(AwsSsmConfig config, String activeProfile) {
        String configuredCommonPath = config.getCommonParameterPath();
        if (StringUtils.hasText(configuredCommonPath)) {
            return ensureTrailingSlash(configuredCommonPath);
        }

        String profile = StringUtils.hasText(activeProfile) ? activeProfile : "local";
        String cleanPrefix = config.getParameterPathPrefix().endsWith("/")
                ? config.getParameterPathPrefix().substring(0, config.getParameterPathPrefix().length() - 1)
                : config.getParameterPathPrefix();
        return String.format("%s/common/%s/", cleanPrefix, profile);
    }

    private String ensureTrailingSlash(String path) {
        return path.endsWith("/") ? path : path + "/";
    }
}

