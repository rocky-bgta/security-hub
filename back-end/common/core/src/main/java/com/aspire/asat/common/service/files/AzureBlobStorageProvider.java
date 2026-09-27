package com.aspire.asat.common.service.files;




import com.aspire.asat.common.config.FileProps;
import com.aspire.asat.common.dto.files.FileUploadResponse;
import com.aspire.asat.common.dto.files.PresignedUrlGenerationRequest;
import com.aspire.asat.common.dto.files.PresignedUrlGenerationResponse;
import com.azure.storage.blob.*;
import com.azure.storage.blob.models.BlobHttpHeaders;
import com.azure.storage.blob.sas.*;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AzureBlobStorageProvider implements StorageProvider {


    private final FileProps props;

    @Override public String providerName() { return "azure"; }

    @Override
    public PresignedUrlGenerationResponse generatePresignedUrl(PresignedUrlGenerationRequest req, String fileId, String key, String contentType) {
        BlobServiceClient svc = new BlobServiceClientBuilder().connectionString(props.getAzures().getStorage().getConnectionString()).buildClient();
        BlobContainerClient container = svc.getBlobContainerClient(props.getAzures().getStorage().getContainerName());
        var blob = container.getBlobClient(key).getBlockBlobClient();

        BlobSasPermission perms = new BlobSasPermission().setCreatePermission(true).setWritePermission(true);
        OffsetDateTime expiry = OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(props.getFiles().getPresignTtlSeconds());
        BlobServiceSasSignatureValues sas = new BlobServiceSasSignatureValues(expiry, perms);

        String url = blob.getBlobUrl() + "?" + blob.generateSas(sas);

        Map<String,String> headers = new LinkedHashMap<>();
        headers.put("x-ms-blob-type", "BlockBlob");
        headers.put("Content-Type", contentType);

        return PresignedUrlGenerationResponse.builder()
                .provider(providerName())
                .fileId(fileId)
                .bucketOrContainer(props.getAzures().getStorage().getContainerName())
                .key(key)
                .method("PUT")
                .url(url)
                .requiredHeaders(headers)
                .expiresAt(Instant.now().plusSeconds(props.getFiles().getPresignTtlSeconds()))
                .build();
    }


    @Override
    public FileUploadResponse uploadFile(String localFilePath, String key) {
        try {
            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(props.getAzures().getStorage().getConnectionString())
                    .buildClient();

            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(props.getAzures().getStorage().getContainerName());
            if (!containerClient.exists()) {
                containerClient.create();
            }

            BlobClient blobClient = containerClient.getBlobClient(key);
            blobClient.uploadFromFile(localFilePath, true);

            // Optionally set headers
            blobClient.setHttpHeaders(new BlobHttpHeaders().setContentType("application/octet-stream"));

            return FileUploadResponse.builder()
                    .provider(providerName())
                    .bucketOrContainer(props.getAzures().getStorage().getContainerName())
                    .path(props.getAzures().getStorage().getContainerName() + "/" + key)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Azure upload failed for " + localFilePath, e);
        }
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
