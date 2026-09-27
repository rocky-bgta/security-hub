package com.aspire.asat.auth.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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

}
