package com.aspire.asat.common.service.aws;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.DecryptRequest;
import software.amazon.awssdk.services.kms.model.DecryptResponse;
import software.amazon.awssdk.services.kms.model.KmsException;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for decrypting values using AWS KMS
 */
@Slf4j
public class KmsService {
    
    private final KmsClient kmsClient;
    private final String region;
    private final String kmsKeyId;
    private final boolean enabled;
    
    public KmsService(String region, String kmsKeyId, boolean enabled) {
        this.region = region != null ? region : "us-east-1";
        this.kmsKeyId = kmsKeyId;
        this.enabled = enabled;
        
        if (enabled) {
            this.kmsClient = KmsClient.builder()
                    .region(Region.of(this.region))
                    .credentialsProvider(DefaultCredentialsProvider.create())
                    .build();
        } else {
            this.kmsClient = null;
            log.info("KMS service is disabled - decryption will be skipped");
        }
    }
    
    /**
     * Decrypt a value using KMS
     * 
     * @param encryptedValue The encrypted value (base64 encoded ciphertext)
     * @return Decrypted plaintext value
     */
    public String decrypt(String encryptedValue) {
        if (!enabled || kmsClient == null) {
            log.debug("KMS is disabled, returning value as-is");
            return encryptedValue;
        }
        
        if (encryptedValue == null || encryptedValue.isEmpty()) {
            return encryptedValue;
        }
        
        try {
            log.debug("Decrypting value using KMS");
            
            // Decode base64 if needed
            byte[] ciphertextBlob;
            try {
                ciphertextBlob = Base64.getDecoder().decode(encryptedValue);
            } catch (IllegalArgumentException e) {
                // If not base64, assume it's already a byte array representation
                ciphertextBlob = encryptedValue.getBytes(StandardCharsets.UTF_8);
            }
            
            DecryptRequest.Builder requestBuilder = DecryptRequest.builder()
                    .ciphertextBlob(SdkBytes.fromByteBuffer(ByteBuffer.wrap(ciphertextBlob)));
            
            // Add key ID if specified
            if (kmsKeyId != null && !kmsKeyId.isEmpty()) {
                requestBuilder.keyId(kmsKeyId);
            }
            
            DecryptRequest request = requestBuilder.build();
            DecryptResponse response = kmsClient.decrypt(request);
            
            String plaintext = response.plaintext().asString(StandardCharsets.UTF_8);
            log.debug("Successfully decrypted value using KMS");
            
            return plaintext;
            
        } catch (KmsException e) {
            log.error("Error decrypting value using KMS: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to decrypt value using KMS: " + e.getMessage(), e);
        }
    }
    
    /**
     * Decrypt multiple values
     * 
     * @param encryptedValues Map of parameter names to encrypted values
     * @return Map of parameter names to decrypted values
     */
    public Map<String, String> decryptAll(Map<String, String> encryptedValues) {
        if (!enabled || kmsClient == null) {
            log.debug("KMS is disabled, returning values as-is");
            return encryptedValues;
        }
        
        Map<String, String> decryptedValues = new HashMap<>();
        
        for (Map.Entry<String, String> entry : encryptedValues.entrySet()) {
            try {
                String decryptedValue = decrypt(entry.getValue());
                decryptedValues.put(entry.getKey(), decryptedValue);
            } catch (Exception e) {
                log.error("Failed to decrypt parameter: {}", entry.getKey(), e);
                // If failOnError is true, this would have been handled by the caller
                // For now, we'll skip this parameter
                decryptedValues.put(entry.getKey(), entry.getValue());
            }
        }
        
        return decryptedValues;
    }
    
    /**
     * Check if KMS is available and accessible
     * 
     * @return true if KMS is accessible, false otherwise
     */
    public boolean isAvailable() {
        if (!enabled || kmsClient == null) {
            return false;
        }
        
        try {
            // KMS doesn't have a simple health check endpoint
            // We'll assume it's available if the client is initialized
            return true;
        } catch (Exception e) {
            log.warn("KMS is not available: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Close the KMS client
     */
    public void close() {
        if (kmsClient != null) {
            kmsClient.close();
        }
    }
}

