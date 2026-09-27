package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.service.SttS3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.ResponseBytes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SttS3ServiceImpl implements SttS3Service {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Override
    public String uploadFile(MultipartFile file, String keyPrefix) {
        String safeName = file.getOriginalFilename() == null ? "audio" : file.getOriginalFilename().replace(" ", "_");
        String key = keyPrefix + "/" + UUID.randomUUID() + "-" + safeName;
        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(file.getContentType())
                            .build(),
                    RequestBody.fromBytes(file.getBytes())
            );
            return key;
        } catch (IOException e) {
            throw new ServiceException("Unable to read uploaded file", HttpStatus.BAD_REQUEST, e);
        } catch (Exception e) {
            throw new ServiceException("Failed to upload audio to S3", HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    @Override
    public File downloadFile(String bucketName, String s3Key) {
        String resolvedBucket = (bucketName == null || bucketName.isBlank()) ? bucket : bucketName;
        try {
            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(
                    GetObjectRequest.builder()
                            .bucket(resolvedBucket)
                            .key(s3Key)
                            .build()
            );
            File tempFile = Files.createTempFile("stt-", resolveSuffixFromKey(s3Key)).toFile();
            Files.write(tempFile.toPath(), objectBytes.asByteArray());
            return tempFile;
        } catch (Exception e) {
            log.error("Failed to download key {} from S3 bucket {}", s3Key, resolvedBucket, e);
            throw new ServiceException("Failed to download audio from S3", HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    private String resolveSuffixFromKey(String s3Key) {
        if (s3Key == null || s3Key.isBlank()) {
            return ".wav";
        }
        int dotIndex = s3Key.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == s3Key.length() - 1) {
            return ".wav";
        }
        String ext = s3Key.substring(dotIndex).toLowerCase();
        if (ext.length() > 10) {
            return ".wav";
        }
        return ext;
    }
}
