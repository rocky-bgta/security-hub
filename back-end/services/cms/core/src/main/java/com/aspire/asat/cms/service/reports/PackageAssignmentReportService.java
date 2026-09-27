package com.aspire.asat.cms.service.reports;

import com.aspire.asat.cms.dto.reports.PackageAssignmentReportDTO;

import java.time.LocalDate;

public interface PackageAssignmentReportService {

    PackageAssignmentReportDTO getPackageAssignmentReport(String clientAdminId,
                                                          String mspId,
                                                          String search,
                                                          String status,
                                                          LocalDate fromDate,
                                                          LocalDate toDate,
                                                          int offset,
                                                          int pageSize);

    byte[] exportPackageAssignmentReportCsv(String clientAdminId,
                                            String mspId,
                                            String search,
                                            String status,
                                            LocalDate fromDate,
                                            LocalDate toDate);
}
