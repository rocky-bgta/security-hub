package com.aspire.asat.registration.data.mspUser.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingProgressDto {
    private Integer totalTrainings;
    private Integer completedTrainings;
    private Integer inProgressTrainings;
    private Double completionPercentage;
}
