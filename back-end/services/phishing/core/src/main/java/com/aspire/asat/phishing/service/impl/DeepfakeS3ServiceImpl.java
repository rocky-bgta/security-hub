package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.service.DeepfakeS3Service;
import com.aspire.asat.phishing.util.DeepfakeImageUtils;
import com.aspire.asat.phishing.util.DeepfakeS3KeyUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeepfakeS3ServiceImpl implements DeepfakeS3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${deepfake.presign-ttl-minutes:60}")
    private long presignTtlMinutes;

    @Override
    public String uploadMultipart(MultipartFile file, String keyPrefix) {
        String safeName = file.getOriginalFilename() == null
                ? "asset"
                : file.getOriginalFilename().replace(" ", "_");
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
            throw new ServiceException("Failed to upload file to S3", HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    @Override
    public String uploadBytes(byte[] content, String keyPrefix, String extension, String contentType) {
        String suffix = extension == null || extension.isBlank() ? "" : (extension.startsWith(".") ? extension : "." + extension);
        String key = keyPrefix + "/" + UUID.randomUUID() + suffix;
        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromBytes(content)
            );
            return key;
        } catch (Exception e) {
            throw new ServiceException("Failed to upload generated media to S3", HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    @Override
    public File downloadToTemp(String s3Key) {
        String key = resolveObjectKey(s3Key);
        try {
            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(
                    GetObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .build()
            );
            File tempFile = Files.createTempFile("deepfake-", resolveSuffixFromKey(key)).toFile();
            Files.write(tempFile.toPath(), objectBytes.asByteArray());
            return tempFile;
        } catch (Exception e) {
            log.error("Failed to download key {} from S3 bucket {}", key, bucket, e);
            throw new ServiceException("Failed to download media from S3", HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    @Override
    public String presignGetUrl(String s3Key) {
        String key = resolveObjectKey(s3Key);
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(presignTtlMinutes))
                    .getObjectRequest(getObjectRequest)
                    .build();
            PresignedGetObjectRequest presigned = s3Presigner.presignGetObject(presignRequest);
            return presigned.url().toString();
        } catch (Exception e) {
            throw new ServiceException("Failed to presign S3 URL", HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    @Override
    public String presignHeyGenCompatibleFaceUrl(String faceKey) {
        String key = resolveObjectKey(faceKey);
        try {
            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(
                    GetObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .build()
            );
            byte[] bytes = objectBytes.asByteArray();
            if (DeepfakeImageUtils.isAvif(bytes)) {
                throw new ServiceException(
                        "Face image is AVIF, which HeyGen does not support. Re-upload the face photo as PNG or JPEG.",
                        HttpStatus.BAD_REQUEST);
            }
            if (DeepfakeImageUtils.detectSupportedFaceContentType(bytes) == null) {
                throw new ServiceException(
                        "Face image must be PNG or JPEG for HeyGen rendering.",
                        HttpStatus.BAD_REQUEST);
            }

            byte[] normalizedJpeg = DeepfakeImageUtils.normalizeToJpegBytes(bytes);
            String normalizedKey = uploadBytes(
                    normalizedJpeg, "deepfake/face-normalized", "jpg", DeepfakeImageUtils.JPEG);
            log.info("Normalized face image for HeyGen: {} -> {}", key, normalizedKey);
            return presignGetUrl(normalizedKey);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Failed to prepare face image for HeyGen", HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    @Override
    public String getBucket() {
        return bucket;
    }

    private String resolveObjectKey(String s3Key) {
        String key = DeepfakeS3KeyUtils.normalizeKey(s3Key, bucket);
        if (key == null || key.isBlank()) {
            throw new ServiceException("S3 object key is required", HttpStatus.BAD_REQUEST);
        }
        if (DeepfakeS3KeyUtils.looksLikeHttpUrl(key)) {
            throw new ServiceException(
                    "Expected an S3 object key but received a URL. Use the backgroundKey/faceKey from the upload response, not the preview URL.",
                    HttpStatus.BAD_REQUEST);
        }
        if (!key.equals(s3Key) && s3Key != null && DeepfakeS3KeyUtils.looksLikeHttpUrl(s3Key.trim())) {
            log.warn("Normalized deepfake media URL to S3 key: {}", key);
        }
        return key;
    }

    private String resolveSuffixFromKey(String s3Key) {
        if (s3Key == null || s3Key.isBlank()) {
            return ".bin";
        }
        int dotIndex = s3Key.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == s3Key.length() - 1) {
            return ".bin";
        }
        String ext = s3Key.substring(dotIndex).toLowerCase();
        if (ext.length() > 10) {
            return ".bin";
        }
        return ext;
    }
}
