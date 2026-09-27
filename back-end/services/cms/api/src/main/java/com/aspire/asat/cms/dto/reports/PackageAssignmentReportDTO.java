package com.aspire.asat.cms.dto.reports;

import com.aspire.asat.cms.dto.apiResponses.AllResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageAssignmentReportDTO {
    private PackageAssignmentSummaryDTO summary;
    private AllResponseDto<List<PackageAssignmentLogRowDTO>> assignmentLog;
}
