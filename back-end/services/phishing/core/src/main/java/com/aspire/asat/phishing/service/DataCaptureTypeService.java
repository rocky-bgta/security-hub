package com.aspire.asat.phishing.service;

import com.aspire.asat.phishing.dto.request.DataCaptureTypeCreateRequest;
import com.aspire.asat.phishing.dto.request.DataCaptureTypeUpdateRequest;
import com.aspire.asat.phishing.dto.response.DataCaptureTypeDto;

import java.util.List;

/**
 * Service for configurable data capture type catalog (Mongo).
 */
public interface DataCaptureTypeService {

    DataCaptureTypeDto createDataCaptureType(DataCaptureTypeCreateRequest request);

    DataCaptureTypeDto updateDataCaptureType(String id, DataCaptureTypeUpdateRequest request);

    void deleteDataCaptureType(String id);

    DataCaptureTypeDto getDataCaptureTypeById(String id);

    List<DataCaptureTypeDto> getDataCaptureTypes(
            String searchParam, boolean isActive, int offset, int pageSize, String sortBy, String sortOrder);

    long countDataCaptureTypes(String searchParam, boolean isActive);
}
