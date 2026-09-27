package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.request.EmailTypeCreateRequest;
import com.aspire.asat.phishing.dto.response.EmailTypeDto;

import java.util.List;

/**
 * Service for managing EmailType configuration entries.
 */
public interface EmailTypeService {

    EmailTypeDto createEmailType(EmailTypeCreateRequest request);

    EmailTypeDto getEmailTypeById(String emailTypeId);

    List<EmailTypeDto> getEmailTypes(String searchParam, boolean isActive,
                                       int offset, int pageSize, String sortBy, String sortOrder);

    long countEmailTypes(String searchParam, boolean isActive);
}

