package com.aspire.asat.cms.repository.custom;

import com.aspire.asat.cms.model.UserSubPackage;

import java.time.LocalDate;
import java.util.List;

public interface UserSubPackageRepositoryCustom {

    long countDistinctParentPackages(String clientAdminId, List<String> clientAdminIds,
                                     LocalDate fromDate, LocalDate toDate);

    long countAssignments(String clientAdminId, List<String> clientAdminIds,
                          String search, String statusFilter,
                          LocalDate fromDate, LocalDate toDate);

    long countByExpiryBucket(String clientAdminId, List<String> clientAdminIds,
                             LocalDate fromDate, LocalDate toDate,
                             String bucket);

    List<UserSubPackage> findAssignmentsForReport(String clientAdminId, List<String> clientAdminIds,
                                                  String search, String statusFilter,
                                                  LocalDate fromDate, LocalDate toDate,
                                                  int offset, int pageSize);

    List<UserSubPackage> findAssignmentsForReportExport(String clientAdminId, List<String> clientAdminIds,
                                                        String search, String statusFilter,
                                                        LocalDate fromDate, LocalDate toDate,
                                                        int offset, int pageSize);

    long countLicenseAssignments(String clientAdminId, List<String> clientAdminIds,
                                 List<String> userIds, String productId, String packageId,
                                 String statusFilter, LocalDate fromDate, LocalDate toDate);

    List<UserSubPackage> findLicenseAssignments(String clientAdminId, List<String> clientAdminIds,
                                                List<String> userIds, String productId, String packageId,
                                                String statusFilter, LocalDate fromDate, LocalDate toDate,
                                                int offset, int pageSize);
}
