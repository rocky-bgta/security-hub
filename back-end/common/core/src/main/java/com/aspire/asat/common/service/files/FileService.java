package com.aspire.asat.common.service.files;



import com.aspire.asat.common.config.FileProps;
import com.aspire.asat.common.dto.files.FileUploadResponse;
import com.aspire.asat.common.dto.files.PresignedUrlGenerationRequest;
import com.aspire.asat.common.dto.files.PresignedUrlGenerationResponse;
import com.aspire.asat.common.util.KeyStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileService {
    private final FileValidationService validator;
    private final FileProps props;
    private final ProviderRegistry registry;

    private StorageProvider getActiveProvider() {
        return registry.get(props.getStorage().getActive());
    }

    public PresignedUrlGenerationResponse prepare(PresignedUrlGenerationRequest req) {
        String contentType = validator.resolveContentType(req);
        StorageProvider provider = getActiveProvider();
        String fileId = UUID.randomUUID().toString();
        String key = "asatv2/uploads/"+KeyStrategy.buildKey(req.getFileType().getFileType(), req.getFilename());
        return provider.generatePresignedUrl(req, fileId, key, contentType);
    }

    public FileUploadResponse fileUpload(String localFilePath, String key) {
        StorageProvider provider = getActiveProvider();
        return provider.uploadFile(localFilePath, key);
    }

    public InputStream readFile(String key) {
        StorageProvider provider = getActiveProvider();
        return provider.readFile(key);
    }

    public String buildUrl(String key) {
        StorageProvider provider = getActiveProvider();
        return provider.buildUrl(key);
    }

    public Map<String, String> generateSignedCookies() {
        try {
            StorageProvider provider = getActiveProvider();
            return provider.generateSignedCookies(Duration.ofHours(20));
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate CloudFront signed cookies", e);
        }
    }

    public String getFilePath(String key) {
        StorageProvider provider = getActiveProvider();
        return provider.getFilePath(key);
    }

    public String getPath(String key) {
        StorageProvider provider = getActiveProvider();
        return provider.getPath(key);
    }

}
