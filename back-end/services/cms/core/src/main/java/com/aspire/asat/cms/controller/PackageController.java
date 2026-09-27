package com.aspire.asat.cms.controller;

import com.aspire.asat.cms.constant.WebApiUrlConstants;
import com.aspire.asat.cms.dto.apiResponses.AllResponseDto;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.clientAdmin.AssignedPackageOverviewWrapperDTO;
import com.aspire.asat.cms.dto.clientAdmin.AvailablePackageWrapperDTO;
import com.aspire.asat.cms.dto.clientAdmin.ClientProductOverviewWrapperDTO;
import com.aspire.asat.cms.dto.clientAdmin.LicenseHistoryWrapperDTO;
import com.aspire.asat.cms.dto.clientAdmin.PackagePerformanceWrapperDTO;
import com.aspire.asat.cms.dto.clientAdmin.ProductAnalyticsDTO;
import com.aspire.asat.cms.dto.common.BulkStatusUpdateRequestDto;
import com.aspire.asat.cms.dto.common.ListOfUUID;
import com.aspire.asat.cms.dto.enums.PackageStatus;
import com.aspire.asat.cms.dto.exam.ExamQuestionResponseDTO;
import com.aspire.asat.cms.dto.packageDto.RequestDto;
import com.aspire.asat.cms.dto.packageDto.ResponseDto;
import com.aspire.asat.cms.dto.packageDto.ResponseDtoWithCourseFeatureAndBundleFeatureDetails;
import com.aspire.asat.cms.dto.packageDto.ResponseDtoWithCourseFeatureDetails;
import com.aspire.asat.cms.dto.packageDto.UpdateRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

@RequestMapping(value = WebApiUrlConstants.PACKAGE_API, produces = "application/json")
public interface PackageController {

    @PostMapping
    ResponseEntity<ApiResponseDto<ResponseDto>> createPackage(@Valid @RequestBody RequestDto requestDto);

    @GetMapping
    ResponseEntity<ApiResponseDto<AllResponseDto<List<ResponseDtoWithCourseFeatureDetails>>>> getAllPackages(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "status", required = false) PackageStatus status,
            @RequestParam(value = "offset", defaultValue = "0") Integer offset,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "order", defaultValue = "desc") String order);


    @GetMapping(WebApiUrlConstants.PATH_VAR_ID)
    ResponseEntity<ApiResponseDto<ResponseDtoWithCourseFeatureAndBundleFeatureDetails>> getPackageById(@PathVariable String id);

    @GetMapping("/details"+WebApiUrlConstants.PATH_VAR_ID)
    ResponseEntity<ApiResponseDto<ResponseDto>> getPackageDetailsById(@PathVariable String id);

    @PutMapping(WebApiUrlConstants.PATH_VAR_ID)
    ResponseEntity<ApiResponseDto<ResponseDto>> updatePackageById(@PathVariable("id") String id, @Valid @RequestBody UpdateRequestDto updateRequestDto);

    @DeleteMapping(WebApiUrlConstants.PATH_VAR_ID)
    ResponseEntity<ApiResponseDto<String>> deletePackageById(@PathVariable String id);

    @GetMapping(value = WebApiUrlConstants.PATH_VAR_EXPORT, produces = "text/csv")
    void exportPackagesToCsv(HttpServletResponse response);

    @DeleteMapping(WebApiUrlConstants.PATH_VAR_BULK_DELETE)
    ResponseEntity<ApiResponseDto<List<String>>> deletePackagesByIds(@Valid @RequestBody ListOfUUID requestDto);

    @PutMapping(WebApiUrlConstants.PATH_VAR_BULK_UPDATE)
    ResponseEntity<ApiResponseDto<List<String>>> bulkUpdatePackagesStatus(@RequestBody BulkStatusUpdateRequestDto requestDto);


    @PostMapping(value = WebApiUrlConstants.PATH_VAR_BULK_EXPORT, produces = "text/csv")
    void exportBulkPackagesToCsv(@Valid @RequestBody ListOfUUID requestDto, HttpServletResponse response);

    @Operation(summary = "Check if package name exists", description = "Returns true if name is available, false if already exists")
    @GetMapping(WebApiUrlConstants.EXISTS)
    ResponseEntity<ApiResponseDto<Boolean>> isPackageNameAvailable(@RequestParam("name") String packageName);

    @Operation(
            summary = "Get products assigned to a client admin",
            description = "Returns client-specific products with progress information"
    )
    @GetMapping(WebApiUrlConstants.CLIENT_PRODUCTS)
    ResponseEntity<ApiResponseDto<ClientProductOverviewWrapperDTO>> getClientProducts(
            @RequestParam String clientAdminId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category
    );

    @Operation(summary = "Get product analytics", description = "Returns product-level analytics for a client admin")
    @GetMapping("/client/product-analytics")
    ResponseEntity<ApiResponseDto<List<ProductAnalyticsDTO>>> getClientProductAnalytics(
            @RequestParam String clientAdminId
    );

    @Operation(summary = "Download product analytics as Excel", description = "Returns downloadable Excel report of product analytics for a client admin")
    @GetMapping("/client/product-analytics/download")
    ResponseEntity<ApiResponseDto<String>> downloadClientProductAnalytics(
            @RequestParam String clientAdminId
    );


    @Operation(
            summary = "Get assigned packages for a client admin",
            description = "Returns assigned packages with user progress, certificate count, topics, and license info"
    )
    @GetMapping(WebApiUrlConstants.CLIENT_ASSIGNED_PACKAGES)
    ResponseEntity<ApiResponseDto<AssignedPackageOverviewWrapperDTO>> assignedPackagesForClientAdmin(
            @RequestParam String clientAdminId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String statusFilter
    );

    @Operation(
            summary = "Get available packages for client admin",
            description = "Returns list of available packages with product name, features, and topic count"
    )
    @GetMapping("/client/packages/available")
    ResponseEntity<ApiResponseDto<AvailablePackageWrapperDTO>> getAvailablePackagesForClientAdmin(
            @RequestParam String clientAdminId
    );


    @Operation(
            summary = "Get license history for client admin",
            description = "Fetch license allocation, renewal, and expiry details"
    )
    @GetMapping("/client/license-history")
    ResponseEntity<ApiResponseDto<LicenseHistoryWrapperDTO>> getLicenseHistoryByClientAdmin(
            @RequestParam String clientAdminId
    );

    @Operation(
            summary = "Get package performance stats for client admin",
            description = "Returns completion rate, time spent, license usage, and certification details"
    )
    @GetMapping("/client/package-performance")
    ResponseEntity<ApiResponseDto<PackagePerformanceWrapperDTO>> getPackagePerformanceStats(
            @RequestParam String clientAdminId
    );

    @Operation(summary = "Upload exam questions from CSV", description = "Parses CSV and returns list of mapped exam questions")
    @PostMapping(value = "/exam/questions/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<ApiResponseDto<List<ExamQuestionResponseDTO>>> uploadQuestions(
            @RequestParam("file") MultipartFile file);







}
