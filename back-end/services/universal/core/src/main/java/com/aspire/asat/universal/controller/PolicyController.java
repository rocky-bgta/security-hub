package com.aspire.asat.universal.controller;

import com.aspire.asat.common.message.MessageKeys;
import com.aspire.asat.common.message.MessageService;
import com.aspire.asat.common.annotation.LogActivity;
import com.aspire.asat.universal.universal.data.apiResponses.ApiResponseDto;
import com.aspire.asat.universal.data.apiresponse.PaginatedResponseDto;
import com.aspire.asat.universal.policy.PolicyDto;
import com.aspire.asat.universal.policy.PolicyRequest;
import com.aspire.asat.universal.service.PolicyService;
import com.aspire.asat.common.enums.ActivityType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/policies")
@RequiredArgsConstructor
public class PolicyController {

    private final PolicyService policyService;
    private final MessageService messageService;


    @GetMapping
    public ResponseEntity<ApiResponseDto<PaginatedResponseDto<PolicyDto>>> getAllPolicies(
            @RequestParam(name = "offset", defaultValue = "0") int offset,
            @RequestParam(name = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(required = false) String policyName) {
        PaginatedResponseDto<PolicyDto> policies = policyService.getAllPolicies(pageSize, offset, policyName);
        ApiResponseDto<PaginatedResponseDto<PolicyDto>> response = new ApiResponseDto<>(
                "Policies retrieved successfully",
                HttpStatus.OK.value(),
                policies
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Get policies for the current user based on role:
     * - Aspire Admin / Super Admin / System User: all policies
     * - Client Admin: if isOwnPolicy=true then only their own policies; otherwise default + MSP + own policies
     * - MSP Admin: if isOwnPolicy=true then only their MSP policies; otherwise default + own + client admin policies
     * - Client User: if isOwnPolicy=true then only their client admin policies; otherwise default (platform) policies and client admin policies
     */
    @GetMapping("/list")
    public ResponseEntity<ApiResponseDto<PaginatedResponseDto<PolicyDto>>> getPoliciesForCurrentUser(
            @RequestParam(name = "offset", defaultValue = "0") int offset,
            @RequestParam(name = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(required = false) String policyName,
            @RequestParam(required = false) String countryId,
            @RequestParam(required = false) String industryId,
            @RequestParam(required = false) Boolean isOwnPolicy) {
        PaginatedResponseDto<PolicyDto> policies = policyService.getPoliciesForCurrentUser(pageSize, offset, policyName, countryId, industryId, isOwnPolicy);
        ApiResponseDto<PaginatedResponseDto<PolicyDto>> response = new ApiResponseDto<>(
                "Policies retrieved successfully",
                HttpStatus.OK.value(),
                policies
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponseDto<PaginatedResponseDto<PolicyDto>>> getActivePolicies(
            @RequestParam(name = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(required = false) String policyName) {
        PaginatedResponseDto<PolicyDto> policies = policyService.getActivePolicies(pageSize, offset, policyName);
        ApiResponseDto<PaginatedResponseDto<PolicyDto>> response = new ApiResponseDto<>(
                "Active policies retrieved successfully",
                HttpStatus.OK.value(),
                policies
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<PolicyDto>> getPolicyById(@PathVariable String id) {
        PolicyDto policy = policyService.getPolicyById(id);
        if (policy != null) {
            ApiResponseDto<PolicyDto> response = new ApiResponseDto<>(
                    "Policy retrieved successfully",
                    HttpStatus.OK.value(),
                    policy
            );
            return ResponseEntity.ok(response);
        }
        ApiResponseDto<PolicyDto> response = new ApiResponseDto<>(
                "Policy not found",
                HttpStatus.NOT_FOUND.value(),
                null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @PostMapping
    @LogActivity(
            activityType = ActivityType.PRODUCT_UPDATED,
            description = "Created policy: #{#policyRequest.policyName != null ? #policyRequest.policyName : 'N/A'}"
    )
    public ResponseEntity<ApiResponseDto<PolicyDto>> createPolicy(@RequestBody PolicyRequest policyRequest) {
        PolicyDto createdPolicy = policyService.createPolicy(policyRequest);
        ApiResponseDto<PolicyDto> response = new ApiResponseDto<>(
                messageService.get(MessageKeys.POLICY_CREATED),
                HttpStatus.CREATED.value(),
                createdPolicy
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @LogActivity(
            activityType = ActivityType.PRODUCT_UPDATED,
            description = "Updated policy: #{#id}",
            oldValueExpression = "#{#id}",
            newValueExpression = "#{#policyRequest.policyName != null ? #policyRequest.policyName : #id}"
    )
    public ResponseEntity<ApiResponseDto<PolicyDto>> updatePolicy(@PathVariable String id,
                                                    @RequestBody PolicyRequest policyRequest) {
        try {
            PolicyDto updatedPolicy = policyService.updatePolicy(id, policyRequest);
            if (updatedPolicy != null) {
                ApiResponseDto<PolicyDto> response = new ApiResponseDto<>(
                        "Policy updated successfully",
                        HttpStatus.OK.value(),
                        updatedPolicy
                );
                return ResponseEntity.ok(response);
            }
            ApiResponseDto<PolicyDto> response = new ApiResponseDto<>(
                    "Policy not found",
                    HttpStatus.NOT_FOUND.value(),
                    null
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (IllegalArgumentException e) {
            ApiResponseDto<PolicyDto> response = new ApiResponseDto<>(
                    e.getMessage(),
                    HttpStatus.UNAUTHORIZED.value(),
                    null
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<String>> deletePolicy(@PathVariable String id) {
        try {
            boolean deleted = policyService.deletePolicy(id);
            if (deleted) {
                ApiResponseDto<String> response = new ApiResponseDto<>(
                        "Policy deleted successfully",
                        HttpStatus.NO_CONTENT.value(),
                        null
                );
                return ResponseEntity.ok(response);
            }
            ApiResponseDto<String> response = new ApiResponseDto<>(
                    "Policy not found",
                    HttpStatus.NOT_FOUND.value(),
                    null
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (IllegalArgumentException e) {
            ApiResponseDto<String> response = new ApiResponseDto<>(
                    e.getMessage(),
                    HttpStatus.UNAUTHORIZED.value(),
                    null
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
}
