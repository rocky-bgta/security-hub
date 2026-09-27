package com.aspire.asat.registration.client.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3UploadService {
    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;


    public String uploadPdf(ByteArrayOutputStream pdfStream, String originalFilename) {
        String objectKey = "invoices/" + UUID.randomUUID().toString() + "_" + originalFilename;
        log.info("Uploading invoice to S3 with key: {}", objectKey);

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType("application/pdf")
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(pdfStream.toByteArray()));
            log.info("Successfully uploaded invoice PDF to S3 bucket '{}'", bucketName);
            return objectKey;
        } catch (S3Exception e) {
            log.error("Error uploading invoice to S3: {}", e.awsErrorDetails().errorMessage(), e);
            throw new RuntimeException("Failed to upload invoice PDF to S3", e);
        }
    }
}

