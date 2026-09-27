package com.aspire.asat.notification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import com.aspire.asat.common.dto.notification.AttachmentDto;

import java.util.List;
import java.util.Map;

@Data
public class EmailDto {
    @NotEmpty(message = "To email address cannot be empty")
    @Email
    private String to;
    @NotEmpty(message = "Subject cannot be empty")
    private String subject;
    private String templateId;
    private Map<String, Object> templateModel;
    private List<AttachmentDto> attachments;
    private Map<String, Object> metadata; // For storing additional metadata like notificationLogId
}
