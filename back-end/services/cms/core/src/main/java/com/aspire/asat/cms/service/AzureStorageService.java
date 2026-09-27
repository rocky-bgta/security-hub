package com.aspire.asat.cms.service;

public interface AzureStorageService {

    String generateUploadUrl(String fileName);
    String extractExpiryTimeFromUrl(String uploadUrl);
    boolean isFileExists(String fileName);
    String getBlobReferenceUrl(String fileName);
}
