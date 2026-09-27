package com.aspire.asat.common.service.aws;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;
import software.amazon.awssdk.services.ssm.model.GetParametersByPathRequest;
import software.amazon.awssdk.services.ssm.model.GetParametersByPathResponse;
import software.amazon.awssdk.services.ssm.model.Parameter;
import software.amazon.awssdk.services.ssm.model.ParameterNotFoundException;
import software.amazon.awssdk.services.ssm.model.SsmException;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for interacting with AWS Systems Manager Parameter Store
 */
@Slf4j
public class SsmService {
    
    private final SsmClient ssmClient;
    private final String region;
    
    public SsmService(String region) {
        this(region, null, null);
    }

    public SsmService(String region, String accessKeyId, String secretAccessKey) {
        this.region = region != null ? region : "us-east-1";
        AwsCredentialsProvider credentialsProvider = buildCredentialsProvider(accessKeyId, secretAccessKey);
        this.ssmClient = SsmClient.builder()
                .region(Region.of(this.region))
                .credentialsProvider(credentialsProvider)
                .build();
    }

    private AwsCredentialsProvider buildCredentialsProvider(String accessKeyId, String secretAccessKey) {
        if (StringUtils.hasText(accessKeyId) && StringUtils.hasText(secretAccessKey)) {
            log.info("Using static AWS credentials for SSM client initialization");
            return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccessKey));
        }
        log.info("Using default AWS credentials provider chain for SSM client initialization");
        return DefaultCredentialsProvider.create();
    }
    
    /**
     * Fetch all parameters under a given path
     * 
     * @param parameterPath The path prefix (e.g., /asat/dev/auth-service/)
     * @return Map of parameter names (without path prefix) to parameter values
     */
    public Map<String, String> fetchParametersByPath(String parameterPath) {
        Map<String, String> parameters = new HashMap<>();
        
        try {
            log.info("Fetching SSM parameters from path: {}", parameterPath);
            
            String nextToken = null;
            int totalFetched = 0;
            
            do {
                GetParametersByPathRequest.Builder requestBuilder = GetParametersByPathRequest.builder()
                        .path(parameterPath)
                        .recursive(false)
                        .withDecryption(true); // Automatically decrypt SecureString parameters
                
                if (nextToken != null) {
                    requestBuilder.nextToken(nextToken);
                }
                
                GetParametersByPathRequest request = requestBuilder.build();
                GetParametersByPathResponse response = ssmClient.getParametersByPath(request);
                
                for (Parameter parameter : response.parameters()) {
                    String fullName = parameter.name();
                    // Remove the path prefix to get just the parameter name
                    String paramName = fullName.substring(parameterPath.length());
                    // Remove leading slash if present
                    if (paramName.startsWith("/")) {
                        paramName = paramName.substring(1);
                    }
                    
                    String value = parameter.value();
                    parameters.put(paramName, value);
                    totalFetched++;
                    
                    log.debug("Fetched parameter: {} (type: {})", paramName, parameter.type());
                }
                
                nextToken = response.nextToken();
                
            } while (nextToken != null);
            
            log.info("Successfully fetched {} parameters from SSM path: {}", totalFetched, parameterPath);
            
        } catch (SsmException e) {
            log.error("Error fetching parameters from SSM path: {}", parameterPath, e);
            throw new RuntimeException("Failed to fetch parameters from SSM: " + e.getMessage(), e);
        }
        
        return parameters;
    }
    
    /**
     * Fetch a single parameter by name
     * 
     * @param parameterName Full parameter name (e.g., /asat/dev/auth-service/database-password)
     * @return Parameter value, or null if not found
     */
    public String fetchParameter(String parameterName) {
        try {
            log.debug("Fetching SSM parameter: {}", parameterName);
            
            GetParameterRequest request = GetParameterRequest.builder()
                    .name(parameterName)
                    .withDecryption(true)
                    .build();
            
            GetParameterResponse response = ssmClient.getParameter(request);
            String value = response.parameter().value();
            
            log.debug("Successfully fetched parameter: {}", parameterName);
            return value;
            
        } catch (ParameterNotFoundException e) {
            log.warn("Parameter not found in SSM: {}", parameterName);
            return null;
        } catch (SsmException e) {
            log.error("Error fetching parameter from SSM: {}", parameterName, e);
            throw new RuntimeException("Failed to fetch parameter from SSM: " + e.getMessage(), e);
        }
    }
    
    /**
     * Check if SSM is available and accessible
     * 
     * @return true if SSM is accessible, false otherwise
     */
    public boolean isAvailable() {
        try {
            // Try to fetch a non-existent parameter to test connectivity
            // This is a lightweight check
            ssmClient.getParameter(GetParameterRequest.builder()
                    .name("/asat/health-check")
                    .build());
            return true;
        } catch (ParameterNotFoundException e) {
            // Parameter doesn't exist, but SSM is accessible
            return true;
        } catch (Exception e) {
            // Missing/invalid credentials is expected when running locally without AWS config
            boolean credentialsIssue = e.getMessage() != null
                    && (e.getMessage().contains("Unable to load credentials")
                            || e.getMessage().contains("credentials"));
            if (credentialsIssue) {
                log.debug("SSM is not available (no AWS credentials): {}", e.getMessage());
            } else {
                log.warn("SSM is not available: {}", e.getMessage());
            }
            return false;
        }
    }
    
    /**
     * Close the SSM client
     */
    public void close() {
        if (ssmClient != null) {
            ssmClient.close();
        }
    }
}

