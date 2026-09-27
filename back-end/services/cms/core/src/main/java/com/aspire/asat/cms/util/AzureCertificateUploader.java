package com.aspire.asat.cms.util;

import com.azure.storage.blob.*;
import com.azure.storage.blob.models.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public class AzureCertificateUploader {

    @Value("${azure.storage.connection-string}")
    private String connectionString;

    @Value("${azure.storage.container-name}")
    private String containerName;

    /**
     * Uploads a certificate file (PDF or image) to Azure Blob Storage.
     *
     * @param fileName name of the file to store
     * @param inputStream content of the file
     * @param size file size in bytes
     * @param contentType e.g., "application/pdf" or "image/png"
     * @return publicly accessible blob URL
     * @author Mahadi Hasan Joy
     * @since 2025-05-20
     */
    public String uploadCertificate(String fileName, InputStream inputStream, long size, String contentType) {
        try {
            BlobServiceClient serviceClient = new BlobServiceClientBuilder()
                    .connectionString(connectionString)
                    .buildClient();

            BlobContainerClient containerClient = serviceClient.getBlobContainerClient(containerName);
            BlobClient blobClient = containerClient.getBlobClient(fileName);

            BlobHttpHeaders headers = new BlobHttpHeaders().setContentType(contentType);

            blobClient.upload(inputStream, size, true);
            blobClient.setHttpHeaders(headers);

            return blobClient.getBlobUrl();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
