package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.enums.PayloadTypeChannel;
import com.aspire.asat.phishing.dto.enums.TemplateType;
import com.aspire.asat.phishing.dto.request.PayloadTypeCreateRequest;
import com.aspire.asat.phishing.dto.request.PayloadTypeUpdateRequest;
import com.aspire.asat.phishing.dto.response.PayloadTypeDto;

import java.util.List;

/**
 * Service for configurable payload type catalog (Mongo).
 */
public interface PayloadTypeService {

    PayloadTypeDto createPayloadType(PayloadTypeCreateRequest request);

    PayloadTypeDto updatePayloadType(String id, PayloadTypeUpdateRequest request);

    void deletePayloadType(String id);

    PayloadTypeDto getPayloadTypeById(String payloadTypeId);

    List<PayloadTypeDto> getPayloadTypes(String searchParam, boolean isActive, int offset, int pageSize,
                                         String sortBy, String sortOrder, PayloadTypeChannel channel);

    long countPayloadTypes(String searchParam, boolean isActive, PayloadTypeChannel channel);

    void validatePayloadTypeForTemplate(TemplateType templateType, PayloadTypeDto payloadType);
}
