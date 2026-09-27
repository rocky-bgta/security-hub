package com.aspire.asat.cms.service;

import com.aspire.asat.cms.dto.apiResponses.AllResponseDto;
import com.aspire.asat.cms.dto.license.LicenseAssignmentExportDto;
import com.aspire.asat.cms.dto.license.LicenseAssignmentRowDto;

import java.time.LocalDate;
import java.util.List;

public interface LicenseAssignmentService {

    AllResponseDto<List<LicenseAssignmentRowDto>> listLicenseAssignments(
            String clientAdminId,
            String mspId,
            String search,
            String packageId,
            String productId,
            String department,
            String status,
            LocalDate fromDate,
            LocalDate toDate,
            int offset,
            int pageSize);

    LicenseAssignmentExportDto exportLicenseAssignments(
            String clientAdminId,
            String mspId,
            String search,
            String packageId,
            String productId,
            String department,
            String status,
            LocalDate fromDate,
            LocalDate toDate,
            String format);
}
