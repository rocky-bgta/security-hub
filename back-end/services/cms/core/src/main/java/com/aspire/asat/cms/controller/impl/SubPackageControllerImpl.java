package com.aspire.asat.cms.controller.impl;

import com.aspire.asat.common.message.MessageKeys;
import com.aspire.asat.common.message.MessageService;
import com.aspire.asat.cms.client.service.ClientAdminServiceClient;
import com.aspire.asat.cms.controller.SubPackageController;
import com.aspire.asat.cms.dto.apiResponses.AllResponseDto;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.client.responseDto.UserSubPackageDetailsResponseDTO;
import com.aspire.asat.cms.dto.subPackage.SubPackageAssignedUserDto;
import com.aspire.asat.cms.dto.subPackage.SubPackageRequestDto;
import com.aspire.asat.cms.dto.subPackage.SubPackageResponseDto;
import com.aspire.asat.cms.dto.subPackage.SubPackageUpdateDto;
import com.aspire.asat.cms.dto.subPackage.SubPackageDetailResponseDto;
import com.aspire.asat.cms.dto.subPackage.TrialSubPackageCreationRequestDto;
import com.aspire.asat.cms.dto.subPackage.TrialSubPackageResponseDto;
import com.aspire.asat.cms.dto.enums.SubPackageStatus;
import com.aspire.asat.cms.service.SubPackageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@Slf4j
public class SubPackageControllerImpl implements SubPackageController {

    private final SubPackageService subPackageService;
    private final MessageService messageService;
    private final ClientAdminServiceClient clientAdminServiceClient;

    public SubPackageControllerImpl(SubPackageService subPackageService,
                                    MessageService messageService,
                                    ClientAdminServiceClient clientAdminServiceClient) {
        this.subPackageService = subPackageService;
        this.messageService = messageService;
        this.clientAdminServiceClient = clientAdminServiceClient;
    }

    @Override
    public ResponseEntity<ApiResponseDto<SubPackageResponseDto>> createSubPackage(SubPackageRequestDto requestDto) {
        SubPackageResponseDto createdSubPackage = subPackageService.createSubPackage(requestDto);
        ApiResponseDto<SubPackageResponseDto> response = new ApiResponseDto<>(
                messageService.get(MessageKeys.PACKAGE_SUB_CREATED), 
                HttpStatus.CREATED.value(), 
                createdSubPackage
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<ApiResponseDto<SubPackageDetailResponseDto>> getSubPackageById(String id) {
        SubPackageDetailResponseDto subPackage = subPackageService.getSubPackageByIdWithDetails(id);
        ApiResponseDto<SubPackageDetailResponseDto> response = new ApiResponseDto<>(
                "SubPackage retrieved successfully with details", 
                HttpStatus.OK.value(), 
                subPackage
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<SubPackageAssignedUserDto>>>> getAssignedUsersBySubPackageId(
            String id, Integer offset, Integer pageSize) {
        try {
            Page<SubPackageAssignedUserDto> page = subPackageService.getAssignedUsersBySubPackageId(
                    id, offset != null ? offset : 0, pageSize != null ? pageSize : 10);
            AllResponseDto<List<SubPackageAssignedUserDto>> allResponseDto = new AllResponseDto<>(
                    offset != null ? offset : 0,
                    pageSize != null ? pageSize : 10,
                    page.getTotalElements(),
                    page.getContent()
            );
            return ResponseEntity.ok(new ApiResponseDto<>(
                    "Assigned users retrieved successfully",
                    HttpStatus.OK.value(),
                    allResponseDto
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Error retrieving assigned users for subPackageId={}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve assigned users: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<SubPackageResponseDto>>>> getAllSubPackages(
            String search, 
            Integer offset, 
            Integer pageSize, 
            String sortBy, 
            String order,
            SubPackageStatus status,
            String productId,
            String clientId,
            String mspId
    ) {
        List<String> clientAdminIds = resolveClientAdminIdsByMspId(mspId);
        if (StringUtils.hasText(mspId) && (clientAdminIds == null || clientAdminIds.isEmpty())) {
            log.info("No client admins found for mspId={}, returning empty sub-package list", mspId);
            AllResponseDto<List<SubPackageResponseDto>> emptyResponse = new AllResponseDto<>(
                    offset, pageSize, 0L, Collections.emptyList()
            );
            return new ResponseEntity<>(
                    new ApiResponseDto<>("SubPackages retrieved successfully", HttpStatus.OK.value(), emptyResponse),
                    HttpStatus.OK
            );
        }

        List<SubPackageResponseDto> subPackages = subPackageService.getAllSubPackagesWithFilters(
                search, status, clientId, productId, clientAdminIds, offset, pageSize, sortBy, order
        );
        
        long totalCount = subPackageService.getSubPackageCountWithFilters(
                search, status, clientId, productId, clientAdminIds
        );
        
        AllResponseDto<List<SubPackageResponseDto>> allResponseDto = new AllResponseDto<>(
                offset, pageSize, totalCount, subPackages
        );
        
        ApiResponseDto<AllResponseDto<List<SubPackageResponseDto>>> response = new ApiResponseDto<>(
                "SubPackages retrieved successfully", 
                HttpStatus.OK.value(), 
                allResponseDto
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * When mspId is provided, resolve all client admin IDs belonging to that MSP.
     * Returns null when mspId is absent so existing single-client filters still apply.
     */
    private List<String> resolveClientAdminIdsByMspId(String mspId) {
        if (!StringUtils.hasText(mspId)) {
            return null;
        }
        List<String> clientAdminIds = clientAdminServiceClient.getClientAdminIdsByMspId(mspId.trim());
        log.info("Resolved {} clientAdminIds for mspId={}",
                clientAdminIds != null ? clientAdminIds.size() : 0, mspId);
        return clientAdminIds != null ? clientAdminIds : Collections.emptyList();
    }

    @Override
    public ResponseEntity<ApiResponseDto<Boolean>> isSubPackageNameExistsForClient(String subPackageName, String clientAdminId) {
        boolean exists = subPackageService.isSubPackageNameExistsForClient(subPackageName, clientAdminId);
        return ResponseEntity.ok(new ApiResponseDto<>("Check completed", HttpStatus.OK.value(), exists));
    }

    @Override
    public ResponseEntity<ApiResponseDto<SubPackageResponseDto>> updateSubPackage(String id, SubPackageUpdateDto updateDto) {
        SubPackageResponseDto updatedSubPackage = subPackageService.updateSubPackage(id, updateDto);
        ApiResponseDto<SubPackageResponseDto> response = new ApiResponseDto<>(
                messageService.get(MessageKeys.PACKAGE_SUB_UPDATED), 
                HttpStatus.OK.value(), 
                updatedSubPackage
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<SubPackageResponseDto>> deleteSubPackage(String id) {
        SubPackageResponseDto deletedSubPackage = subPackageService.deleteSubPackage(id);
        ApiResponseDto<SubPackageResponseDto> response = new ApiResponseDto<>(
                "SubPackage deleted successfully", 
                HttpStatus.OK.value(), 
                deletedSubPackage
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<UserSubPackageDetailsResponseDTO>> getUserSubPackageDetails(String userId, String subPackageId) {
        log.info("Getting user subpackage details for userId: {} and subPackageId: {}", userId, subPackageId);
        UserSubPackageDetailsResponseDTO response = subPackageService.getUserSubPackageDetails(userId, subPackageId);
        return ResponseEntity.ok(new ApiResponseDto<>("User subpackage details retrieved successfully", 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<TrialSubPackageResponseDto>>>> getAllTrialSubPackages(
            String search,
            Integer offset,
            Integer pageSize,
            String sortBy,
            String order,
            SubPackageStatus status,
            String productId,
            String clientAdminId
    ) {
        log.info("Getting all trial sub-packages with filters - search: {}, status: {}, productId: {}, clientAdminId: {}, offset: {}, pageSize: {}",
                search, status, productId, clientAdminId, offset, pageSize);

        // Use the service method to get trial sub-packages
        List<TrialSubPackageResponseDto> trialSubPackages = subPackageService.getAllTrialSubPackages(
                search, status, clientAdminId, productId, offset, pageSize, sortBy, order
        );

        long totalCount = subPackageService.getTrialSubPackageCount(
                search, status, clientAdminId, productId
        );

        AllResponseDto<List<TrialSubPackageResponseDto>> allResponseDto = new AllResponseDto<>(
                offset, pageSize, totalCount, trialSubPackages
        );

        ApiResponseDto<AllResponseDto<List<TrialSubPackageResponseDto>>> response = new ApiResponseDto<>(
                "Trial SubPackages retrieved successfully",
                HttpStatus.OK.value(),
                allResponseDto
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<SubPackageResponseDto>> createTrialSubPackage(TrialSubPackageCreationRequestDto requestDto) {
        log.info("Creating trial sub-package for productId: {}, packageId: {}, clientAdminId: {}",
                requestDto.getProductId(), requestDto.getPackageId(), requestDto.getClientAdminId());
        SubPackageResponseDto createdSubPackage = subPackageService.createTrialSubPackage(requestDto);
        ApiResponseDto<SubPackageResponseDto> response = new ApiResponseDto<>(
                "Trial sub-package created successfully",
                HttpStatus.CREATED.value(),
                createdSubPackage
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<SubPackageResponseDto>>> getSubPackagesByPackageIds(
            com.aspire.asat.cms.dto.subPackage.SubPackagesByPackageIdsRequest request) {
        List<SubPackageResponseDto> subPackages = subPackageService.getSubPackagesByPackageIds(
                request.getPackageIds(), request.getClientAdminId());
        ApiResponseDto<List<SubPackageResponseDto>> response = new ApiResponseDto<>(
                "Sub-packages retrieved successfully",
                HttpStatus.OK.value(),
                subPackages
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
