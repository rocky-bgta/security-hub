package com.aspire.asat.cms.controller.impl;

import com.aspire.asat.cms.controller.FeatureController;
import com.aspire.asat.cms.dto.apiResponses.AllResponseDto;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.common.BulkStatusUpdateRequestDto;
import com.aspire.asat.cms.dto.common.ListOfUUID;
import com.aspire.asat.cms.dto.featureDto.RequestDto;
import com.aspire.asat.cms.dto.featureDto.ResponseDto;
import com.aspire.asat.cms.dto.featureDto.ResponseDtoWithPackageDetails;
import com.aspire.asat.cms.dto.featureDto.UpdateRequestDto;
import com.aspire.asat.cms.service.FeatureService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
public class FeatureControllerImpl implements FeatureController {

    private final FeatureService featureService;

    public FeatureControllerImpl(FeatureService featureService) {
        this.featureService = featureService;
    }

    @Override
    public ResponseEntity<ApiResponseDto<ResponseDto>> createFeature(@Valid @RequestBody RequestDto requestDto) {
        ResponseDto savedFeature = featureService.saveFeature(requestDto);
        ApiResponseDto<ResponseDto> response = new ApiResponseDto<>("Feature created successfully", HttpStatus.CREATED.value(), savedFeature);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<ResponseDto>>>> getAllFeatures(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize,
            @RequestParam(value = "sortBy", defaultValue = "createdAt", required = false) String sortBy,
            @RequestParam(value = "order", defaultValue = "desc", required = false) String order) {
        List<ResponseDto> responseDtos = featureService.getAllFeatures(search, offset, pageSize, sortBy, order);
        AllResponseDto<List<ResponseDto>> allResponseDto = new AllResponseDto<>(offset,pageSize,featureService.getTotalFeatureCount(),responseDtos);
        ApiResponseDto<AllResponseDto<List<ResponseDto>>> response = new ApiResponseDto<>("Products retrieved successfully", HttpStatus.OK.value(), allResponseDto);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<ResponseDto>>>> getAllFeaturesByStatus(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize,
            @RequestParam(value = "sortBy", defaultValue = "createdAt", required = false) String sortBy,
            @RequestParam(value = "order", defaultValue = "desc", required = false) String order) {
        List<ResponseDto> responseDtos = featureService.getAllFeaturesByStatus(search, offset, pageSize, sortBy, order);
        AllResponseDto<List<ResponseDto>> allResponseDto = new AllResponseDto<>(offset,pageSize,featureService.getTotalFeatureCount(),responseDtos);
        ApiResponseDto<AllResponseDto<List<ResponseDto>>> response = new ApiResponseDto<>("Features retrieved successfully", HttpStatus.OK.value(), allResponseDto);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @Override
    public ResponseEntity<ApiResponseDto<ResponseDtoWithPackageDetails>> getFeatureById(@PathVariable String id) {
        ResponseDtoWithPackageDetails responseDto = featureService.getFeatureById(id);
        ApiResponseDto<ResponseDtoWithPackageDetails> response = new ApiResponseDto<>("Feature retrieved successfully", HttpStatus.OK.value(), responseDto);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<ResponseDto>> updateFeatureById(@PathVariable("id") String id, @Valid @RequestBody UpdateRequestDto updateRequestDto) {
        ResponseDto updatedFeature = featureService.updateFeatureById(id, updateRequestDto);
        ApiResponseDto<ResponseDto> response = new ApiResponseDto<>("Feature updated successfully", HttpStatus.OK.value(), updatedFeature);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<String>> deleteFeatureById(@PathVariable String id) {
        String deletedFeatureName = featureService.deleteFeatureById(id);
        ApiResponseDto<String> response = new ApiResponseDto<>(
                "Feature deleted successfully", HttpStatus.OK.value(), "Deleted Feature: " + deletedFeatureName
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public void exportFeaturesToCsv(HttpServletResponse response) {
        featureService.exportFeatures(response);
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<String>>> deleteFeaturesByIds(@Valid @RequestBody ListOfUUID requestDto) {
        List<String> deletedFeatureNames = featureService.deleteFeaturesByIds(requestDto.getIds());
        ApiResponseDto<List<String>> response = new ApiResponseDto<>("Features deleted successfully", HttpStatus.OK.value(), deletedFeatureNames);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<String>>> bulkUpdateFeaturesStatus(@RequestBody BulkStatusUpdateRequestDto requestDto) {
        List<String> updatedFeatureNames = featureService.updateFeaturesStatusByIds(
                requestDto.getIds(),
                requestDto.getStatus()
        );
        ApiResponseDto<List<String>> response = new ApiResponseDto<>(
                "Features updated successfully", HttpStatus.OK.value(), updatedFeatureNames);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public void exportBulkFeaturesToCsv(@RequestBody ListOfUUID requestDto, HttpServletResponse response) {
        featureService.exportBulkFeatures(requestDto.getIds(), response);
    }
}
