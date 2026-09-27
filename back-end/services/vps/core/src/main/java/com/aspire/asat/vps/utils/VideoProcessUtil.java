package com.aspire.asat.vps.utils;

import com.aspire.asat.vps.dto.enums.VideoStatus;
import com.aspire.asat.vps.dto.response.RequestDto;
import com.aspire.asat.vps.dto.response.ResponseDto;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Instant;

public class VideoProcessUtil {
    public static void validateRequest(RequestDto requestDto) {
        if (requestDto.getId() == null || requestDto.getId().toString().isEmpty()) {
            throw new IllegalArgumentException("Content ID cannot be null or empty");
        }
        if (requestDto.getContentType() == null || requestDto.getContentType().isEmpty()) {
            throw new IllegalArgumentException("Content type cannot be null or empty");
        }
        if (requestDto.getVideoUrl() == null || requestDto.getVideoUrl().isEmpty()) {
            throw new IllegalArgumentException("Video URL cannot be null or empty");
        }
    }

    public static ResponseDto prepareInitialResponse(RequestDto requestDto) {
        return ResponseDto.builder()
                .id(requestDto.getId())
                .specificContentId(requestDto.getSpecificContentId())
                .contentType(requestDto.getContentType())
                .processingStatus(requestDto.getProcessingStatus())
                .videoUrl(requestDto.getVideoUrl())
                .processedVideoUrl(requestDto.getProcessedVideoUrl())
                .userId(requestDto.getUserId())
                .uploadedFiles(null)
                .uploadedAt(Instant.now())
                .build();
    }

    public static boolean isValidUrl(String urlString) {
        try {
            // This checks for valid URL format
            URL url = new URL(urlString);

            // This checks for valid URI encoding (e.g., spaces, & etc.)
            url.toURI();

            return true;
        } catch (MalformedURLException | URISyntaxException e) {
            System.err.println("Invalid URL: " + e.getMessage());
            return false;
        }
    }

}
