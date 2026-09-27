package com.aspire.asat.cms.dto.certificateTemplate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DynamicFieldsDto {
    private boolean showLearnerName;
    private boolean showCourseName;
    private boolean showIssueDate;
    private boolean showCertificateId;
    private boolean showQrCode;
}
