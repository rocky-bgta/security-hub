package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.response.DeepfakeVideoStepResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface DeepfakeFaceService {

    /**
     * Provide exactly one of {@code faceImage} or {@code faceImageId}.
     */
    DeepfakeVideoStepResponse updateStep2(UUID videoId, MultipartFile faceImage, UUID faceImageId);
}
