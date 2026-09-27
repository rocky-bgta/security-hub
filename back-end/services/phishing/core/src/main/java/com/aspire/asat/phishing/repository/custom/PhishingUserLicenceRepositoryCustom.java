package com.aspire.asat.phishing.repository.custom;

import com.aspire.asat.phishing.dto.enums.RiskGroup;
import com.aspire.asat.phishing.dto.response.LicensedUserDepartmentCountDto;
import com.aspire.asat.phishing.dto.response.LicensedUserGroupCountDto;
import com.aspire.asat.phishing.model.PhishingUserLicence;

import java.util.List;

/**
 * Paged queries and aggregations over {@code phishing_user_licence}.
 */
public interface PhishingUserLicenceRepositoryCustom {

    List<PhishingUserLicence> findLicensedUsers(
            String clientAdminId,
            String productPackageId,
            String search,
            List<String> departments,
            List<RiskGroup> riskGroups,
            int offset,
            int pageSize);

    long countLicensedUsers(
            String clientAdminId,
            String productPackageId,
            String search,
            List<String> departments,
            List<RiskGroup> riskGroups);

    List<LicensedUserDepartmentCountDto> countByDepartment(
            String clientAdminId, String productPackageId);

    /**
     * Counts licence rows grouped by snapshotted {@code riskGroup}
     * (Registration risk-group chip shape).
     */
    List<LicensedUserGroupCountDto> countByGroup(
            String clientAdminId, String productPackageId);

    /**
     * Distinct licensed userIds for a client + product package (projection only).
     */
    List<String> findLicensedUserIds(String clientAdminId, String productPackageId);

    /**
     * Unpaged active licensed users for Step 6 audience selection.
     * Optional filters: departments (id or name), groups (groupIds intersection or riskGroup enum),
     * userIds. Null/empty optional lists mean "no filter" for that dimension — callers pass
     * only the dimensions needed for the audience type.
     */
    List<PhishingUserLicence> findAudienceLicences(
            String clientAdminId,
            String productPackageId,
            List<String> departmentIds,
            List<String> groupIds,
            List<String> userIds);

    /**
     * Updates licence-relevant profile fields on all rows matching userId + clientAdminId.
     *
     * @return number of documents matched
     */
    long updateUserSnapshot(
            String userId,
            String clientAdminId,
            String firstName,
            String lastName,
            String phoneNumber,
            String departmentName,
            String countryName,
            Boolean active);
}
