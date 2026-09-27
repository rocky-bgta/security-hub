package com.aspire.asat.registration.service.impl;

import com.aspire.asat.registration.service.BlobStorageService;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class BlobStorageServiceImpl implements BlobStorageService {

    private final BlobContainerClient blobContainerClient;

    public BlobStorageServiceImpl(@Value("${azure.storage.connection-string}") String connectionString,
                                  @Value("${azure.storage.container-name}") String containerName) {
        this.blobContainerClient = new BlobContainerClientBuilder()
                .connectionString(connectionString)
                .containerName(containerName)
                .buildClient();
    }

    public String uploadLogo(String role, UUID id, MultipartFile file) throws IOException {
        String fileName = role+id;
        BlobClient blobClient = blobContainerClient.getBlobClient(fileName);
        BlobHttpHeaders headers = new BlobHttpHeaders().setContentType(file.getContentType());
        blobClient.upload(file.getInputStream(), file.getSize(), true);
        blobClient.setHttpHeaders(headers);
        return blobClient.getBlobUrl();
    }
}


