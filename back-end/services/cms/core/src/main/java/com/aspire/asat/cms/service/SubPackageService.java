package com.aspire.asat.cms.service;

import com.aspire.asat.cms.dto.client.responseDto.UserSubPackageDetailsResponseDTO;
import com.aspire.asat.cms.dto.subPackage.SubPackageAssignedUserDto;
import com.aspire.asat.cms.dto.subPackage.SubPackageDetailResponseDto;
import com.aspire.asat.cms.dto.subPackage.SubPackageRequestDto;
import com.aspire.asat.cms.dto.subPackage.SubPackageResponseDto;
import com.aspire.asat.cms.dto.subPackage.SubPackageUpdateDto;
import com.aspire.asat.cms.dto.subPackage.TrialSubPackageCreationRequestDto;
import com.aspire.asat.cms.dto.subPackage.TrialSubPackageResponseDto;
import com.aspire.asat.cms.dto.enums.SubPackageStatus;
import org.springframework.data.domain.Page;

import java.util.List;

public interface SubPackageService {

    // Create sub-package
    SubPackageResponseDto createSubPackage(SubPackageRequestDto requestDto);

    // Get sub-package by ID
    SubPackageResponseDto getSubPackageById(String id);

    // Get sub-package by ID with related details
    SubPackageDetailResponseDto getSubPackageByIdWithDetails(String id);

    // Get all sub-packages with dynamic filtering
    List<SubPackageResponseDto> getAllSubPackagesWithFilters(
            String search,
            SubPackageStatus status,
            String clientId,
            String productId,
            List<String> clientAdminIds,
            Integer offset,
            Integer pageSize,
            String sortBy,
            String order
    );

    // Get total count with dynamic filtering
    long getSubPackageCountWithFilters(
            String search,
            SubPackageStatus status,
            String clientId,
            String productId,
            List<String> clientAdminIds
    );

    boolean isSubPackageNameExistsForClient(String subPackageName, String clientAdminId);

    // Update sub-package
    SubPackageResponseDto updateSubPackage(String id, SubPackageUpdateDto updateDto);

    // Delete sub-package
    SubPackageResponseDto deleteSubPackage(String id);

    // Client-facing user subpackage details method
    UserSubPackageDetailsResponseDTO getUserSubPackageDetails(String userId, String subPackageId);

    // Get all trial sub-packages with showInSite field
    List<TrialSubPackageResponseDto> getAllTrialSubPackages(
            String search,
            SubPackageStatus status,
            String clientId,
            String productId,
            Integer offset,
            Integer pageSize,
            String sortBy,
            String order
    );

    // Get total count of trial sub-packages
    long getTrialSubPackageCount(
            String search,
            SubPackageStatus status,
            String clientId,
            String productId
    );

    // Create trial sub-package
    SubPackageResponseDto createTrialSubPackage(TrialSubPackageCreationRequestDto requestDto);

    /**
     * Fetch sub-packages for the given package IDs. When multiple sub-packages share a package ID,
     * a client-specific match is preferred when clientAdminId is provided.
     */
    List<SubPackageResponseDto> getSubPackagesByPackageIds(java.util.List<String> packageIds, String clientAdminId);

    /**
     * Get paginated users assigned to a sub-package, enriched with AspireUser email/fullName.
     * Ordered by assignedDate descending.
     */
    Page<SubPackageAssignedUserDto> getAssignedUsersBySubPackageId(String subPackageId, int offset, int pageSize);
}
