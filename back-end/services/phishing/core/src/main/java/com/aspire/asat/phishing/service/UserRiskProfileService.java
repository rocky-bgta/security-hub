package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.UserRiskProfileSaveRequestDto;
import com.aspire.asat.phishing.dto.response.UserRiskSummaryDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service for managing UserRiskProfile persistence.
 */
public interface UserRiskProfileService {

    /**
     * Saves a user risk profile (create or update by clientId + userId).
     *
     * @param request the profile data to save
     * @return the saved profile as UserRiskSummaryDto
     */
    UserRiskSummaryDto save(UserRiskProfileSaveRequestDto request);

    /**
     * Creates a new user risk profile (always inserts; does not update existing).
     *
     * @param request the profile data to create
     */
    void createRiskProfile(UserRiskProfileSaveRequestDto request);

    /**
     * Gets a user risk profile by id.
     *
     * @param id the profile id
     * @return the profile as UserRiskSummaryDto, or null if not found
     */
    UserRiskSummaryDto getById(String id);

    /**
     * Updates a user risk profile by id. Only non-null fields in request are applied.
     *
     * @param id      the profile id
     * @param request the fields to update
     * @return the updated profile as UserRiskSummaryDto, or null if not found
     */
    UserRiskSummaryDto updateById(String id, UserRiskProfileSaveRequestDto request);

    /**
     * Gets a paginated list of user risk profiles with optional search and filters.
     *
     * @param clientAdminId filter by client (maps to clientId)
     * @param department     filter by department (optional)
     * @param search         search in email and firstName (optional)
     * @param offset         pagination offset
     * @param pageSize       page size
     * @param sortBy         sort field (e.g. riskScore, email, firstName)
     * @param sortOrder      asc or desc
     * @return paginated list and total count
     */
    PageResult<UserRiskSummaryDto> getList(String clientAdminId, String department, String search,
                                          int offset, int pageSize, String sortBy, String sortOrder);

    /**
     * Exports user risk profiles in CSV format using the same filters as getList.
     *
     * @param clientAdminId required client identifier (maps to clientId)
     * @param department optional department filter
     * @param search optional search filter (email, firstName)
     * @return CSV bytes
     */
    byte[] exportCsv(String clientAdminId, String department, String search);

    /**
     * Returns the list of userIds (from the given list) that have a risk profile for the client.
     * Used by Registration service to enrich end-user list with isRiskProfileExist.
     */
    List<String> getExistingUserIds(String clientId, List<String> userIds);

    /**
     * Simple page result for list API.
     */
    record PageResult<T>(List<T> items, long total, int offset, int pageSize) {}
}
