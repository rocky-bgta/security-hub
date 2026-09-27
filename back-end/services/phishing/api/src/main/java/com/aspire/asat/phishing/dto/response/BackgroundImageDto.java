package com.aspire.asat.phishing.dto.response;

import com.aspire.asat.phishing.dto.enums.BackgroundImageStatus;
import com.aspire.asat.phishing.dto.enums.DeepfakeImageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackgroundImageDto {

    private UUID id;

    private String fileName;

    /** S3 object key; pass as backgroundKey in Step 1 when backgroundType is CUSTOM. */
    private String fileKey;

    /** Time-limited presigned URL for previewing the background image. */
    private String url;

    private DeepfakeImageType imageType;

    private boolean isActive;

    private boolean isGlobal;

    private BackgroundImageStatus status;

    private Instant createdAt;
}
