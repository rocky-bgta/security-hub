package com.aspire.asat.billing.utils.file;

import com.azure.storage.blob.*;
import com.azure.storage.blob.models.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public class AzureBlobUploader {

    @Value("${azure.storage.connection-string}")
    private String connectionString;

    @Value("${azure.storage.container-name}")
    private String containerName;

    /**
     * Uploads a file to Azure Blob Storage.
     * @author Mahadi Hasan Joy
     * @since  2023-04-10
     * @param fileName
     * @param inputStream
     * @param size
     * @param contentType
     * @return
     */
    public String uploadFile(String fileName, InputStream inputStream, long size, String contentType) {
        try {
            // Build service and container client
            BlobServiceClient serviceClient = new BlobServiceClientBuilder()
                    .connectionString(connectionString)
                    .buildClient();

            BlobContainerClient containerClient = serviceClient.getBlobContainerClient(containerName);
            BlobClient blobClient = containerClient.getBlobClient(fileName);

            // Set HTTP headers
            BlobHttpHeaders headers = new BlobHttpHeaders().setContentType(contentType);

            // Upload the file
            blobClient.upload(inputStream, size, true);
            blobClient.setHttpHeaders(headers);

            return blobClient.getBlobUrl();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
