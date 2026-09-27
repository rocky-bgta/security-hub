package com.aspire.asat.cms.repository.custom;

import com.aspire.asat.cms.dto.enums.Status;
import com.aspire.asat.cms.model.CertificateTemplate;

import java.util.List;

public interface CertificateTemplateRepositoryCustom {

    /**
     * Find certificate templates with pagination and search
     *
     * @param search   Search term for template name (optional)
     * @param status   Filter by status (optional)
     * @param offset   Number of records to skip
     * @param pageSize Number of records to return
     * @param sortBy   Field to sort by
     * @param order    Sort order (asc/desc)
     * @return List of certificate templates
     */
    List<CertificateTemplate> findAllWithPaginationAndSearch(
            String search,
            Status status,
            int offset,
            int pageSize,
            String sortBy,
            String order
    );

    /**
     * Count certificate templates matching the search criteria
     *
     * @param search Search term for template name (optional)
     * @param status Filter by status (optional)
     * @return Total count of matching templates
     */
    long countWithSearch(String search, Status status);

    /**
     * Find certificate templates excluding default and assigned templates with pagination and search
     *
     * @param search           Search term for template name (optional)
     * @param status           Filter by status (optional)
     * @param excludeIds       List of template IDs to exclude (default and assigned template IDs)
     * @param offset           Number of records to skip
     * @param pageSize         Number of records to return
     * @param sortBy           Field to sort by
     * @param order            Sort order (asc/desc)
     * @return List of certificate templates excluding default and assigned
     */
    List<CertificateTemplate> findAllExcludingIdsWithPaginationAndSearch(
            String search,
            Status status,
            List<String> excludeIds,
            int offset,
            int pageSize,
            String sortBy,
            String order
    );

    /**
     * Count certificate templates excluding default and assigned templates matching the search criteria
     *
     * @param search     Search term for template name (optional)
     * @param status     Filter by status (optional)
     * @param excludeIds List of template IDs to exclude (default and assigned template IDs)
     * @return Total count of matching templates
     */
    long countExcludingIdsWithSearch(String search, Status status, List<String> excludeIds);
}

