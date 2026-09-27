package com.aspire.asat.cms.controller;

import com.aspire.asat.cms.constant.WebApiUrlConstants;
import com.aspire.asat.cms.dto.apiResponses.AllResponseDto;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.common.BulkStatusUpdateRequestDto;
import com.aspire.asat.cms.dto.common.ListOfUUID;
import com.aspire.asat.cms.dto.featureDto.RequestDto;
import com.aspire.asat.cms.dto.featureDto.ResponseDto;
import com.aspire.asat.cms.dto.featureDto.ResponseDtoWithPackageDetails;
import com.aspire.asat.cms.dto.featureDto.UpdateRequestDto;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RequestMapping(value = WebApiUrlConstants.FEATURE_API, produces = "application/json")
public interface FeatureController {

    @PostMapping
    ResponseEntity<ApiResponseDto<ResponseDto>> createFeature(@Valid @RequestBody RequestDto requestDto);

    @GetMapping
    ResponseEntity<ApiResponseDto<AllResponseDto<List<ResponseDto>>>> getAllFeatures(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize,
            @RequestParam(value = "sortBy", defaultValue = "createdAt", required = false) String sortBy,
            @RequestParam(value = "order", defaultValue = "desc", required = false) String order);

    @GetMapping(WebApiUrlConstants.PATH_VAR_ENABLED)
    ResponseEntity<ApiResponseDto<AllResponseDto<List<ResponseDto>>>> getAllFeaturesByStatus(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "offset", defaultValue = "0", required = false) Integer offset,
            @RequestParam(value = "pageSize", defaultValue = "10", required = false) Integer pageSize,
            @RequestParam(value = "sortBy", defaultValue = "createdAt", required = false) String sortBy,
            @RequestParam(value = "order", defaultValue = "desc", required = false) String order);

    @GetMapping(WebApiUrlConstants.PATH_VAR_ID)
    ResponseEntity<ApiResponseDto<ResponseDtoWithPackageDetails>> getFeatureById(@PathVariable String id);

    @PutMapping(WebApiUrlConstants.PATH_VAR_ID)
    ResponseEntity<ApiResponseDto<ResponseDto>> updateFeatureById(@PathVariable("id") String id, @Valid @RequestBody UpdateRequestDto updateRequestDto);

    @DeleteMapping(WebApiUrlConstants.PATH_VAR_ID)
    ResponseEntity<ApiResponseDto<String>> deleteFeatureById(@PathVariable String id);

    @GetMapping(value = WebApiUrlConstants.PATH_VAR_EXPORT, produces = "text/csv")
    void exportFeaturesToCsv(HttpServletResponse response);

    @DeleteMapping(WebApiUrlConstants.PATH_VAR_BULK_DELETE)
    ResponseEntity<ApiResponseDto<List<String>>> deleteFeaturesByIds(@Valid @RequestBody ListOfUUID requestDto);

    @PutMapping(WebApiUrlConstants.PATH_VAR_BULK_UPDATE)
    ResponseEntity<ApiResponseDto<List<String>>> bulkUpdateFeaturesStatus(@RequestBody BulkStatusUpdateRequestDto requestDto);

    @PostMapping(value = WebApiUrlConstants.PATH_VAR_BULK_EXPORT, produces = "text/csv")
    void exportBulkFeaturesToCsv(@Valid @RequestBody ListOfUUID requestDto, HttpServletResponse response);
}
