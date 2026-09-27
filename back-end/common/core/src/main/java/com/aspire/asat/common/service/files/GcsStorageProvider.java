package com.aspire.asat.common.service.files;






import com.aspire.asat.common.config.FileProps;
import com.aspire.asat.common.dto.files.FileUploadResponse;
import com.aspire.asat.common.dto.files.PresignedUrlGenerationRequest;
import com.aspire.asat.common.dto.files.PresignedUrlGenerationResponse;
import com.google.cloud.storage.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.Duration;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GcsStorageProvider implements StorageProvider {
    private final Storage storage;
    private final FileProps props;

    @Override public String providerName() { return "gcs"; }

    @Override
    public PresignedUrlGenerationResponse generatePresignedUrl(PresignedUrlGenerationRequest req, String fileId, String key, String contentType) {

        return PresignedUrlGenerationResponse.builder()
                .provider(providerName())
                .fileId(fileId)
                .bucketOrContainer(props.getGcp().getBucket())
                .key(key)
                .method("PUT")
                .url("")
                .requiredHeaders(null)
                .expiresAt(null)
                .build();
    }

    @Override
    public FileUploadResponse uploadFile(String localFilePath,  String key) {
        return null;
    }

    @Override
    public InputStream readFile(String containerOrBucket, String key) {
        return null;
    }

    @Override
    public InputStream readFile(String key) {
        return null;
    }

    @Override
    public String buildUrl(String key) {
        return "";
    }

    @Override
    public Map<String, String> generateSignedCookies(Duration validFor) {
        return Map.of();
    }

    @Override
    public String getFilePath(String key) {
        return "";
    }

    @Override
    public String getPath(String key) {
        return "";
    }


}
