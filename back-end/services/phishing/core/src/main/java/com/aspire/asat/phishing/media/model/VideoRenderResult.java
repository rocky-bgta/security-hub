package com.aspire.asat.phishing.media.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Output of a video render. When {@code videoBytes} is present the caller is
 * expected to persist it to storage; otherwise {@code videoUrl} is used as-is
 * (e.g. a placeholder produced by a stub engine).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoRenderResult {
    private byte[] videoBytes;
    private String contentType;
    private String videoUrl;
    private byte[] thumbnailBytes;
    private String thumbnailContentType;
    private String heygenAvatarId;
    private String heygenAvatarFaceKey;
}
