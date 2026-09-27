package com.aspire.asat.registration.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

public interface BlobStorageService {

    String uploadLogo(String role, UUID id, MultipartFile file) throws IOException;

}
