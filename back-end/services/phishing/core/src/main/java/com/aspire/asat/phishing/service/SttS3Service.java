package com.aspire.asat.phishing.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public interface SttS3Service {
    String uploadFile(MultipartFile file, String keyPrefix);
    File downloadFile(String bucketName, String s3Key);
}
