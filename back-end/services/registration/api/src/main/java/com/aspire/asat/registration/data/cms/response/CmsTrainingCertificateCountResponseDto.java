package com.aspire.asat.registration.data.cms.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmsTrainingCertificateCountResponseDto {

    private long trainingCompletedCount;
    private long certificateEarnedCount;
}
