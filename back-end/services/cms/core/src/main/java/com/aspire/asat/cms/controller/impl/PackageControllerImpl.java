package com.aspire.asat.cms.controller.impl;

import com.aspire.asat.common.annotation.LogActivity;
import com.aspire.asat.cms.controller.PackageController;
import com.aspire.asat.cms.dto.apiResponses.AllResponseDto;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.clientAdmin.*;
import com.aspire.asat.cms.dto.common.BulkStatusUpdateRequestDto;
import com.aspire.asat.cms.dto.common.ListOfUUID;
import com.aspire.asat.cms.dto.enums.PackageStatus;
import com.aspire.asat.cms.dto.exam.ExamQuestionResponseDTO;
import com.aspire.asat.cms.dto.packageDto.*;
import com.aspire.asat.cms.service.ExamQuestionService;
import com.aspire.asat.cms.service.PackageService;
import com.aspire.asat.common.enums.ActivityType;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

@RestController
public class PackageControllerImpl implements PackageController {

    private final PackageService packageService;
    private final ExamQuestionService examQuestionService;

    public PackageControllerImpl(PackageService packageService, ExamQuestionService examQuestionService) {
        this.packageService = packageService;
        this.examQuestionService = examQuestionService;
    }

    @Override
    @LogActivity(
            activityType = ActivityType.PRODUCT_UPDATED,
            description = "Created package: #{#requestDto.name != null ? #requestDto.name : 'N/A'}"
    )
    public ResponseEntity<ApiResponseDto<ResponseDto>> createPackage(@Valid @RequestBody RequestDto requestDto) {
        ResponseDto savedPackage = packageService.savePackage(requestDto);
        ApiResponseDto<ResponseDto> response = new ApiResponseDto<>("Package created successfully", HttpStatus.CREATED.value(), savedPackage);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<ResponseDtoWithCourseFeatureDetails>>>> getAllPackages(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "status", required = false) PackageStatus status,
            @RequestParam(value = "offset", defaultValue = "0") Integer offset,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "order", defaultValue = "desc") String order) {

        List<ResponseDtoWithCourseFeatureDetails> responseDtos = packageService.getAllPackagesNew(search, status, offset, pageSize, sortBy, order);
        long total = packageService.getTotalPackageCount(search, status);
        AllResponseDto<List<ResponseDtoWithCourseFeatureDetails>> allResponseDto =
                new AllResponseDto<>(offset, pageSize, total, responseDtos);

        ApiResponseDto<AllResponseDto<List<ResponseDtoWithCourseFeatureDetails>>> response =
                new ApiResponseDto<>("Packages retrieved successfully", HttpStatus.OK.value(), allResponseDto);

        return ResponseEntity.ok(response);
    }


    @Override
    public ResponseEntity<ApiResponseDto<ResponseDtoWithCourseFeatureAndBundleFeatureDetails>> getPackageById(@PathVariable String id) {
        ResponseDtoWithCourseFeatureAndBundleFeatureDetails responseDto = packageService.getPackageById(id);
        ApiResponseDto<ResponseDtoWithCourseFeatureAndBundleFeatureDetails> response = new ApiResponseDto<>("Package retrieved successfully", HttpStatus.OK.value(), responseDto);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<ResponseDto>> getPackageDetailsById(String id) {
        ResponseDto packageDetails = packageService.getPackageDetailsById(id);
        return ResponseEntity.ok(new ApiResponseDto<>("Package details retrieved successfully", 200, packageDetails));
    }


    @Override
    @LogActivity(
            activityType = ActivityType.PRODUCT_UPDATED,
            description = "Updated package: #{#id}",
            oldValueExpression = "#{#id}",
            newValueExpression = "#{#updateRequestDto.name != null ? #updateRequestDto.name : #id}"
    )
    public ResponseEntity<ApiResponseDto<ResponseDto>> updatePackageById(@PathVariable("id") String id, @Valid @RequestBody UpdateRequestDto updateRequestDto) {
        ResponseDto updatedPackage = packageService.updatePackageById(id,updateRequestDto);
        ApiResponseDto<ResponseDto> response = new ApiResponseDto<>("Package updated successfully", HttpStatus.OK.value(), updatedPackage);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<String>> deletePackageById(@PathVariable String id) {
        String deletedPackageName = packageService.deletePackageById(id);
        ApiResponseDto<String> response = new ApiResponseDto<>(
                "Package deleted successfully", HttpStatus.OK.value(), "Deleted Package: " + deletedPackageName
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public void exportPackagesToCsv(HttpServletResponse response) {
        packageService.exportPackages(response);
    }
    @Override
    public ResponseEntity<ApiResponseDto<List<String>>> deletePackagesByIds(@Valid @RequestBody ListOfUUID requestDto) {
        List<String> deletedPackageNames = packageService.deletePackagesByIds(requestDto.getIds());
        ApiResponseDto<List<String>> response = new ApiResponseDto<>("Packages deleted successfully", HttpStatus.OK.value(), deletedPackageNames);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<String>>> bulkUpdatePackagesStatus(@RequestBody BulkStatusUpdateRequestDto requestDto) {
        List<String> updatedPackageNames = packageService.updatePackagesStatusByIds(
                requestDto.getIds(),
                requestDto.getStatus()
        );
        ApiResponseDto<List<String>> response = new ApiResponseDto<>(
                "Packages updated successfully", HttpStatus.OK.value(), updatedPackageNames);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public void exportBulkPackagesToCsv(@RequestBody ListOfUUID requestDto, HttpServletResponse response) {
        packageService.exportBulkPackages(requestDto.getIds(), response);
    }

    @Override
    public ResponseEntity<ApiResponseDto<Boolean>> isPackageNameAvailable(@RequestParam("name") String packageName) {
        boolean available = !packageService.packageExistsByName(packageName); // reverse it, return true if not exists
        return ResponseEntity.ok(new ApiResponseDto<>("Check completed", 200, available));
    }

    @Override
    public ResponseEntity<ApiResponseDto<ClientProductOverviewWrapperDTO>> getClientProducts(
            String clientAdminId,
            String search,
            String category
    ) {
        ClientProductOverviewWrapperDTO productWrapper = packageService.getClientProducts(clientAdminId, search, category);

        ApiResponseDto<ClientProductOverviewWrapperDTO> response =
                new ApiResponseDto<>("Client products retrieved successfully", HttpStatus.OK.value(), productWrapper);

        return ResponseEntity.ok(response);
    }


    @Override
    public ResponseEntity<ApiResponseDto<List<ProductAnalyticsDTO>>> getClientProductAnalytics(String clientAdminId) {
        List<ProductAnalyticsDTO> analytics = packageService.getClientProductAnalytics(clientAdminId);
        return ResponseEntity.ok(new ApiResponseDto<>("Product analytics fetched successfully", HttpStatus.OK.value(), analytics));
    }


    @Override
    public ResponseEntity<ApiResponseDto<String>> downloadClientProductAnalytics(String clientAdminId) {
        String downloadUrl = packageService.downloadClientProductAnalytics(clientAdminId);
        return ResponseEntity.ok(new ApiResponseDto<>("Product analytics Excel generated", HttpStatus.OK.value(), downloadUrl));
    }

    @Override
    public ResponseEntity<ApiResponseDto<AssignedPackageOverviewWrapperDTO>> assignedPackagesForClientAdmin(
            String clientAdminId, String search, String statusFilter) {

        AssignedPackageOverviewWrapperDTO wrapper = packageService.getAssignedPackagesForClientAdmin(clientAdminId, search, statusFilter);

        ApiResponseDto<AssignedPackageOverviewWrapperDTO> response = new ApiResponseDto<>(
                "Assigned packages retrieved successfully",
                HttpStatus.OK.value(),
                wrapper
        );

        return ResponseEntity.ok(response);
    }


    @Override
    public ResponseEntity<ApiResponseDto<AvailablePackageWrapperDTO>> getAvailablePackagesForClientAdmin(String clientAdminId) {
        AvailablePackageWrapperDTO wrapper = packageService.getAvailablePackagesForClientAdmin(clientAdminId);
        ApiResponseDto<AvailablePackageWrapperDTO> response = new ApiResponseDto<>(
                "Available packages retrieved successfully", HttpStatus.OK.value(), wrapper
        );
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ApiResponseDto<LicenseHistoryWrapperDTO>> getLicenseHistoryByClientAdmin(String clientAdminId) {
        LicenseHistoryWrapperDTO historyWrapper = packageService.getLicenseHistoryByClientAdmin(clientAdminId);
        ApiResponseDto<LicenseHistoryWrapperDTO> response = new ApiResponseDto<>(
                "License history retrieved successfully", HttpStatus.OK.value(), historyWrapper
        );
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<ApiResponseDto<PackagePerformanceWrapperDTO>> getPackagePerformanceStats(String clientAdminId) {
        PackagePerformanceWrapperDTO performanceData = packageService.getPackagePerformanceStats(clientAdminId);
        return ResponseEntity.ok(new ApiResponseDto<>("Performance stats fetched successfully", 200, performanceData));
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<ExamQuestionResponseDTO>>> uploadQuestions(MultipartFile file) {
        List<ExamQuestionResponseDTO> questions = examQuestionService.parseExamQuestionsFromCSV(file);
        return ResponseEntity.ok(new ApiResponseDto<>("Questions uploaded successfully", 200, questions));
    }




}
