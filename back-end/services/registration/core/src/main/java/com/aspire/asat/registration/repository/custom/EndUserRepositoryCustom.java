package com.aspire.asat.registration.repository.custom;

import com.aspire.asat.registration.data.clientAdmin.request.AdminStatus;
import com.aspire.asat.registration.model.AspireUser;
import org.bson.Document;

import java.util.List;

public interface EndUserRepositoryCustom {

    List<Document> getFilteredEndUsers(String clientAdminId, String search, AdminStatus status, String department, int offset, int pageSize);

    long countFilteredEndUsers(String clientAdminId, String search, AdminStatus status, String department);

    List<Document> findUnassignedEndUsers(String clientAdminId, String productId, String search, AdminStatus status, int offset, int pageSize);
    long countUnassignedEndUsers(String clientAdminId, String productId, String search, AdminStatus status);

    /**
     * Find all AspireUsers with advanced filters
     * @param search Optional search by email (username field)
     * @param userType Optional filter by userType
     * @param country Optional filter by country
     * @param mspId Optional filter by MSP ID
     * @param clientAdminId Optional filter by client admin ID
     * @param status Optional filter by status
     * @param offset Page offset
     * @param pageSize Page size
     * @return List of AspireUser entities
     */
    List<AspireUser> findAllAspireUsersWithFilters(
            String search,
            String userType,
            String country,
            String mspId,
            String clientAdminId,
            String status,
            int offset,
            int pageSize);

    /**
     * Count all AspireUsers with advanced filters
     * @param search Optional search by email (username field)
     * @param userType Optional filter by userType
     * @param country Optional filter by country
     * @param mspId Optional filter by MSP ID
     * @param clientAdminId Optional filter by client admin ID
     * @param status Optional filter by status
     * @return Total count of users matching the filters
     */
    long countAllAspireUsersWithFilters(
            String search,
            String userType,
            String country,
            String mspId,
            String clientAdminId,
            String status);
}
