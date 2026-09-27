package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.AllResponseDto;
import com.aspire.asat.phishing.dto.enums.RiskGroup;
import com.aspire.asat.phishing.dto.request.AllocateLicenceRequest;
import com.aspire.asat.phishing.dto.response.AllocateLicenceResponseDto;
import com.aspire.asat.phishing.dto.response.LicensedUserDepartmentCountDto;
import com.aspire.asat.phishing.dto.response.LicensedUserDto;
import com.aspire.asat.phishing.dto.response.LicensedUserGroupCountDto;

import java.util.List;

public interface PhishingUserLicenceService {

    /**
     * Preview or confirm unique-user license allocation for a draft campaign's productPackageId.
     */
    AllocateLicenceResponseDto allocateLicence(String campaignId, AllocateLicenceRequest request);

    /**
     * Paginated licensed users for the campaign's clientAdminId + productPackageId.
     * {@code offset} is a page index (skip = offset * pageSize).
     */
    AllResponseDto<List<LicensedUserDto>> listLicensedUsers(
            String campaignId,
            String search,
            List<String> departments,
            List<RiskGroup> riskGroups,
            int offset,
            int pageSize);

    List<LicensedUserDepartmentCountDto> getLicensedUserDepartmentCounts(String campaignId);

    List<LicensedUserGroupCountDto> getLicensedUserGroupCounts(String campaignId);
}
