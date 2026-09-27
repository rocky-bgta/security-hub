package com.aspire.asat.cms.controller.impl;

import com.aspire.asat.common.message.MessageKeys;
import com.aspire.asat.common.message.MessageService;
import com.aspire.asat.cms.controller.UserSubPackageController;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.user.UserSubPackageAssignRequest;
import com.aspire.asat.cms.service.user_operations.ClientUserOperationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class UserSubPackageControllerImpl implements UserSubPackageController {

    private final ClientUserOperationService clientUserOperationService;
    private final MessageService messageService;

    @Autowired
    public UserSubPackageControllerImpl(ClientUserOperationService clientUserOperationService,
                                        MessageService messageService) {
        this.clientUserOperationService = clientUserOperationService;
        this.messageService = messageService;
    }

    @Override
    public ResponseEntity<ApiResponseDto<Void>> assignSubPackagesToUsers(UserSubPackageAssignRequest request) {
        log.info("Received request to assign subpackages to users: {} subpackages", 
                request.getSubPackageData().size());
        
        try {
            clientUserOperationService.assignSubPackagesToUsers(request);
            return ResponseEntity.ok(new ApiResponseDto<>(messageService.get(MessageKeys.PACKAGE_SUB_ASSIGNED), 200, null));
        } catch (Exception e) {
            log.error("Error assigning subpackages to users", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to assign subpackages: " + e.getMessage(), 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<Void>> resetUserSubPackage(String userId, String subPackageId) {
        log.info("Received request to reset user subpackage progress for userId: {} and subPackageId: {}", userId, subPackageId);
        
        try {
            clientUserOperationService.resetUserSubPackage(userId, subPackageId);
            return ResponseEntity.ok(new ApiResponseDto<>("User subpackage progress reset successfully", 200, null));
        } catch (Exception e) {
            log.error("Error resetting user subpackage progress for userId: {} and subPackageId: {}", userId, subPackageId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to reset user subpackage progress: " + e.getMessage(), 500, null));
        }
    }
}
