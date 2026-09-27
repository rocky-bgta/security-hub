package com.aspire.asat.common.service.files;


import com.aspire.asat.common.dto.files.FileUploadResponse;
import com.aspire.asat.common.dto.files.PresignedUrlGenerationRequest;
import com.aspire.asat.common.dto.files.PresignedUrlGenerationResponse;

import java.io.InputStream;
import java.time.Duration;
import java.util.Map;

public interface StorageProvider {
    String providerName(); // "s3" | "azure" | "gcs"
    PresignedUrlGenerationResponse generatePresignedUrl(PresignedUrlGenerationRequest req, String fileId, String key, String contentType);
    FileUploadResponse uploadFile(String localFilePath, String key);
    InputStream readFile(String containerOrBucket, String key);
    InputStream readFile(String key);
    String buildUrl(String key);
    Map<String, String> generateSignedCookies(Duration validFor);
    String getFilePath(String key);
    String getPath(String key);

}
