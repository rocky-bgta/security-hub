package com.aspire.asat.cms.service.impl;

import com.aspire.asat.cms.service.AzureStorageService;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.PublicAccessType;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
public class AzureStorageServiceImpl implements AzureStorageService {


    @Value("${azure.storage.connection-string}")
    private String connectionString;

    @Value("${azure.storage.container-name}")
    private String containerName;

    @Override
    public String generateUploadUrl(String fileName) {
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();

        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
        BlobClient blobClient = containerClient.getBlobClient(fileName);

        BlobSasPermission permission = new BlobSasPermission().setWritePermission(true);
        OffsetDateTime expiryTime = OffsetDateTime.now(ZoneOffset.UTC).plusMinutes(15);

        BlobServiceSasSignatureValues values = new BlobServiceSasSignatureValues(expiryTime, permission);
        String sasToken = blobClient.generateSas(values);

        return blobClient.getBlobUrl() + "?" + sasToken;
    }
    @Override
    public String extractExpiryTimeFromUrl(String uploadUrl) {
        try {
            URI uri = new URI(uploadUrl);
            String query = uri.getQuery();
            for (String param : query.split("&")) {
                if (param.startsWith("se=")) {
                    return URLDecoder.decode(param.split("=")[1], StandardCharsets.UTF_8);
                }
            }
        } catch (Exception e) {
            return "Unknown";
        }
        return "Unknown";
    }
    @Override
    public boolean isFileExists(String fileName) {
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
        BlobClient blobClient = containerClient.getBlobClient(fileName);
        return blobClient.exists();
    }
    @Override
    public String getBlobReferenceUrl(String fileName) {
        // Build the client
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);

        // Set container to allow public read access to blobs
        containerClient.setAccessPolicy(PublicAccessType.BLOB, null);

        // Get blob client
        BlobClient blobClient = containerClient.getBlobClient(fileName);

        // Set permissions: read, write, delete, list, create
        BlobSasPermission permission = new BlobSasPermission()
                .setReadPermission(true)    // Allow read access
                .setWritePermission(true)   // Allow write access
                .setDeletePermission(true)  // Allow delete access
                .setListPermission(true)    // Allow list access
                .setCreatePermission(true); // Allow create access

        // Adjust the expiry time to be later than the start time (buffered time)
        OffsetDateTime startTime = OffsetDateTime.now(ZoneOffset.UTC);
        OffsetDateTime expiryTime = startTime.plusYears(10);  // Set expiry time 10 years ahead of start time

        // Generate SAS Token with corrected start and expiry times
        BlobServiceSasSignatureValues values = new BlobServiceSasSignatureValues(expiryTime, permission).setStartTime(startTime);
        String sasToken = blobClient.generateSas(values);

        // Return full URL + SAS (public access with SAS token for additional operations)
        return blobClient.getBlobUrl() + "?" + sasToken;
    }

}
