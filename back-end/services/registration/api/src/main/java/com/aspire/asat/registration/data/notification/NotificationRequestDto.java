package com.aspire.asat.registration.data.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequestDto {
    private String to;
    private String subject;
    private String templateId;
    private Map<String, Object> templateModel;
    private List<AttachmentDto> attachments;
}
