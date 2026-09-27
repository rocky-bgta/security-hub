package com.aspire.asat.billing.utils.file;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.util.UUID;

/**
 * Utility for uploading invoice PDFs to AWS S3
 * Used for email attachments via Notification Service
 */
@Slf4j
@Component
public class S3InvoiceUploader {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    public S3InvoiceUploader(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    /**
     * Upload invoice PDF to S3
     *
     * @param pdfBytes PDF content as byte array
     * @param invoiceId Invoice ID for filename
     * @return S3 object key (path)
     */
    public String uploadInvoicePdf(byte[] pdfBytes, String invoiceId) {
        String objectKey = "invoices/" + UUID.randomUUID().toString() + "_Invoice_" + invoiceId + ".pdf";
        log.info("Uploading invoice PDF to S3 with key: {}", objectKey);

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType("application/pdf")
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(pdfBytes));
            log.info("Successfully uploaded invoice PDF to S3 bucket '{}' with key '{}'", bucketName, objectKey);
            return objectKey;
        } catch (S3Exception e) {
            log.error("Error uploading invoice PDF to S3: {}", e.awsErrorDetails().errorMessage(), e);
            throw new RuntimeException("Failed to upload invoice PDF to S3", e);
        }
    }
}

