package com.aspire.asat.registration.repository.custom;

import com.aspire.asat.registration.data.clientAdmin.request.AdminStatus;
import com.aspire.asat.registration.model.ClientAdmin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;

public interface ClientAdminRepositoryCustom {

    /**
     * Find client admins with filtering and pagination
     * @param search Search keyword for organization name, domain, or email
     * @param mspId MSP ID filter
     * @param status Status filter
     * @param createdAt Creation date filter
     * @param country Country filter
     * @param state State filter
     * @param pageable Pagination parameters
     * @return Page of ClientAdmin entities
     */
    Page<ClientAdmin> findClientAdminsWithFilters(
            String search,
            String mspId,
            AdminStatus status,
            Instant createdAt,
            String country,
            String state,
            Pageable pageable
    );

    /**
     * Count total client admins matching the filters
     * @param search Search keyword for organization name, domain, or email
     * @param mspId MSP ID filter
     * @param status Status filter
     * @param createdAt Creation date filter
     * @param country Country filter
     * @param state State filter
     * @return Total count
     */
    long countClientAdminsWithFilters(
            String search,
            String mspId,
            AdminStatus status,
            Instant createdAt,
            String country,
            String state
    );

}
