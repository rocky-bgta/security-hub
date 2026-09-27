package com.aspire.asat.cms.service;

import com.aspire.asat.cms.dto.clientCertificateTemplate.ClientCertificateTemplateReqDto;
import com.aspire.asat.cms.dto.clientCertificateTemplate.ClientCertificateTemplateRespDto;

public interface ClientCertificateTemplateService {

    /**
     * Create or update client certificate template.
     * If an active template exists for the client, it will be updated.
     * If no active template exists, a new one will be created.
     *
     * @param reqDto The request DTO containing template data
     * @return The created or updated client certificate template response DTO
     */
    ClientCertificateTemplateRespDto createOrUpdateTemplate(ClientCertificateTemplateReqDto reqDto);

    /**
     * Update the active status of a client certificate template
     *
     * @param templateId The ID of the client certificate template to update
     * @param active     The new active status
     * @return The updated client certificate template response DTO
     */
    ClientCertificateTemplateRespDto updateStatus(String templateId, boolean active);

    /**
     * Get the client certificate template for the current client admin
     *
     * @return The client certificate template response DTO
     */
    ClientCertificateTemplateRespDto getClientCertificateTemplate();

    /**
     * Get the client certificate template by ID
     *
     * @param templateId The ID of the client certificate template
     * @return The client certificate template response DTO
     */
    ClientCertificateTemplateRespDto getById(String templateId);

    /**
     * Get the client certificate template by client admin user ID
     *
     * @param userId The user ID of the client admin
     * @return The client certificate template response DTO
     */
    ClientCertificateTemplateRespDto getByClientAdminUserId();
}

