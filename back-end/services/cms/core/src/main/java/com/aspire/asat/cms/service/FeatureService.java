package com.aspire.asat.cms.service;

import com.aspire.asat.cms.dto.enums.Status;
import com.aspire.asat.cms.dto.featureDto.RequestDto;
import com.aspire.asat.cms.dto.featureDto.ResponseDto;
import com.aspire.asat.cms.dto.featureDto.ResponseDtoWithPackageDetails;
import com.aspire.asat.cms.dto.featureDto.UpdateRequestDto;
import com.aspire.asat.cms.model.Feature;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

public interface FeatureService {

    ResponseDto saveFeature(RequestDto requestDto);

    List<ResponseDto> getAllFeatures(String search, Integer offset, Integer pageSize, String sortBy, String order);

    ResponseDtoWithPackageDetails getFeatureById(String id);

    ResponseDto updateFeatureById(String featureId,UpdateRequestDto updateRequestDto);

    String deleteFeatureById(String id);

    void exportFeatures(HttpServletResponse response);

    long getTotalFeatureCount();

    List<String> deleteFeaturesByIds(List<String> ids);

    List<String> updateFeaturesStatusByIds(List<String> ids, Status status);

    void exportBulkFeatures(List<String> ids, HttpServletResponse response);

    List<ResponseDto> getAllFeaturesByStatus(String search, Integer offset, Integer pageSize, String sortBy, String order);
}
