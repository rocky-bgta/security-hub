package com.aspire.asat.cms.service;

import com.aspire.asat.cms.dto.certificateTemplate.CertificateTemplateReqDto;
import com.aspire.asat.cms.dto.certificateTemplate.CertificateTemplateRespDto;
import com.aspire.asat.cms.dto.certificateTemplate.ClientAdminCertificateTemplateRespDto;
import com.aspire.asat.cms.dto.enums.Status;

import java.util.List;

public interface CertificateTemplateService {

    /**
     * Create a new certificate template
     *
     * @param reqDto The request DTO containing template data
     * @return The created certificate template response DTO
     */
    CertificateTemplateRespDto createTemplate(CertificateTemplateReqDto reqDto);

    /**
     * Update an existing certificate template
     *
     * @param templateId The ID of the template to update
     * @param reqDto     The request DTO containing updated template data
     * @return The updated certificate template response DTO
     */
    CertificateTemplateRespDto updateTemplate(String templateId, CertificateTemplateReqDto reqDto);

    /**
     * Get a certificate template by ID
     *
     * @param templateId The ID of the template to retrieve
     * @return The certificate template response DTO
     */
    CertificateTemplateRespDto getTemplateById(String templateId);

    /**
     * Get all certificate templates (without pagination)
     *
     * @return List of all certificate templates
     */
    List<CertificateTemplateRespDto> getAllTemplates();

    /**
     * Get certificate templates with pagination and search
     *
     * @param search   Search term for template name (optional)
     * @param status   Filter by status (optional)
     * @param offset   Number of records to skip
     * @param pageSize Number of records to return
     * @param sortBy   Field to sort by
     * @param order    Sort order (asc/desc)
     * @return List of certificate templates matching criteria
     */
    List<CertificateTemplateRespDto> getTemplatesWithPagination(
            String search,
            Status status,
            Integer offset,
            Integer pageSize,
            String sortBy,
            String order
    );

    /**
     * Get total count of certificate templates matching search criteria
     *
     * @param search Search term for template name (optional)
     * @param status Filter by status (optional)
     * @return Total count of matching templates
     */
    long getTotalTemplateCount(String search, Status status);

    /**
     * Update the status of a certificate template
     *
     * @param templateId The ID of the template to update
     * @param status     The new status
     * @return The updated certificate template response DTO
     */
    CertificateTemplateRespDto updateTemplateStatus(String templateId, Status status);

    /**
     * Delete a certificate template by ID
     *
     * @param templateId The ID of the template to delete
     * @return The deleted certificate template response DTO
     */
    CertificateTemplateRespDto deleteTemplate(String templateId);

    /**
     * Get the default certificate template.
     *
     * @return The default certificate template response DTO
     */
    CertificateTemplateRespDto getDefaultTemplate();

    /**
     * Get certificate templates for client admin with assignment status.
     * Always includes default and assigned templates exclusively.
     * Other templates are returned with pagination.
     * If the client admin has a ClientCertificateTemplate, that template will have isAssigned = true.
     * Otherwise, the default template will have isAssigned = true.
     *
     * @param search   Search term for template name (optional)
     * @param offset   Number of records to skip
     * @param pageSize Number of records to return
     * @param sortBy   Field to sort by
     * @param order    Sort order (asc/desc)
     * @return List of certificate templates with isAssigned flag
     */
    List<ClientAdminCertificateTemplateRespDto> getClientAdminTemplates(
            String search,
            Integer offset,
            Integer pageSize,
            String sortBy,
            String order
    );

    /**
     * Get total count of certificate templates for client admin (excluding default and assigned)
     *
     * @param search Search term for template name (optional)
     * @return Total count of matching templates
     */
    long getClientAdminTemplatesCount(String search);

    /**
     * Check if certificate template is configured for a client
     *
     * @param clientId The client admin ID to check
     * @return true if certificate template exists for the client, false otherwise
     */
    boolean checkCertificateTemplateExists(String clientId);
}
