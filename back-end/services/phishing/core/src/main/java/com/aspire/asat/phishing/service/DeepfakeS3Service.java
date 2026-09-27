package com.aspire.asat.phishing.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * Storage helper for deepfake media assets (face images, synthesized audio,
 * rendered video).
 */
public interface DeepfakeS3Service {

    String uploadMultipart(MultipartFile file, String keyPrefix);

    String uploadBytes(byte[] content, String keyPrefix, String extension, String contentType);

    File downloadToTemp(String s3Key);

    /**
     * @return a time-limited presigned GET URL for the given key
     */
    String presignGetUrl(String s3Key);

    /**
     * @return a time-limited presigned GET URL with PNG/JPEG content type for HeyGen
     */
    String presignHeyGenCompatibleFaceUrl(String faceKey);

    String getBucket();
}
