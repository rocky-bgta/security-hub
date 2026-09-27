package com.aspire.asat.common.dto.files;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder 
@NoArgsConstructor
@AllArgsConstructor
public class PresignedUrlGenerationResponse {
    private String provider;               // s3 | azure | gcs
    private String fileId;
    private String bucketOrContainer;
    private String key;                    // object/blob name
    private String method;                 // PUT (S3) — Azure/GCS samples also use PUT
    private String url;
    private String objectUrl;// presigned / SAS / signed URL
    private Map<String,String> requiredHeaders;
    private Instant expiresAt;
}