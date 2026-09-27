package com.aspire.asat.cms.service;

import com.aspire.asat.cms.dto.clientAdmin.*;
import com.aspire.asat.cms.dto.enums.PackageStatus;
import com.aspire.asat.cms.dto.enums.Status;
import com.aspire.asat.cms.dto.packageDto.*;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

public interface PackageService {

    ResponseDto savePackage(RequestDto requestDto);

    List<ResponseDto> getAllPackages(String search, Integer offset, Integer pageSize, String sortBy, String order);

    ResponseDtoWithCourseFeatureAndBundleFeatureDetails getPackageById(String id);

    ResponseDto updatePackageById(String packageId, UpdateRequestDto updateRequestDto);

    String deletePackageById(String id);

    void exportPackages(HttpServletResponse response);

    long getTotalPackageCount(String search, PackageStatus status);


    List<String> deletePackagesByIds(List<String> ids);

    List<String> updatePackagesStatusByIds(List<String> ids, Status status);

    void exportBulkPackages(List<String> ids, HttpServletResponse response);

    boolean packageExistsByName(String packageName);

    List<ResponseDtoWithCourseFeatureDetails> getAllPackagesNew(String search, PackageStatus status, Integer offset, Integer pageSize, String sortBy, String order);

    ResponseDto getPackageDetailsById(String id);

    ClientProductOverviewWrapperDTO getClientProducts(String clientAdminId, String search, String category);

    long getTotalClientProductCount(String clientAdminId, String search, String category);

    List<ProductAnalyticsDTO> getClientProductAnalytics(String clientAdminId);

    String downloadClientProductAnalytics(String clientAdminId);

    AssignedPackageOverviewWrapperDTO getAssignedPackagesForClientAdmin(String clientAdminId, String search, String statusFilter);

    AvailablePackageWrapperDTO getAvailablePackagesForClientAdmin(String clientAdminId);

    LicenseHistoryWrapperDTO getLicenseHistoryByClientAdmin(String clientAdminId);

    PackagePerformanceWrapperDTO getPackagePerformanceStats(String clientAdminId);


}
