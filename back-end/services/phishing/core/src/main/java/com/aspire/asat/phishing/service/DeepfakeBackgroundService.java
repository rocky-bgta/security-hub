package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.enums.DeepfakeImageType;
import com.aspire.asat.phishing.dto.response.BackgroundImageDto;
import com.aspire.asat.phishing.dto.response.BackgroundUploadResponse;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

public interface DeepfakeBackgroundService {

    BackgroundUploadResponse uploadBackground(MultipartFile backgroundImage);

    /**
     * Returns images visible to the current user (own uploads and/or globals),
     * optionally filtered by imageType and other criteria, paginated.
     */
    Page<BackgroundImageDto> listBackgrounds(int offset, int pageSize, String fileName,
                                             LocalDate uploadDate, Boolean isActive, Boolean isGlobal,
                                             DeepfakeImageType imageType);
}
