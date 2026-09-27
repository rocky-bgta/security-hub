package com.aspire.asat.common.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Attachment DTO for email notifications
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentDto {
    private String bucketName;
    private String objectKey;
}