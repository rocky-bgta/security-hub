package com.aspire.asat.universal.controller;

import com.aspire.asat.universal.universal.data.apiResponses.ApiResponseDto;
import com.aspire.asat.universal.data.apiresponse.PaginatedResponseDto;
import com.aspire.asat.universal.policy.PolicyTypeDto;
import com.aspire.asat.universal.policy.PolicyTypeRequest;
import com.aspire.asat.universal.service.PolicyTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/policy-types")
@RequiredArgsConstructor
public class PolicyTypeController {

    private final PolicyTypeService policyTypeService;


    @GetMapping
    public ResponseEntity<ApiResponseDto<PaginatedResponseDto<PolicyTypeDto>>> getAllPolicyTypes(
            @RequestParam(name = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "0") int offset) {
        PaginatedResponseDto<PolicyTypeDto> policyTypes = policyTypeService.getAllPolicyTypes(pageSize, offset);
        ApiResponseDto<PaginatedResponseDto<PolicyTypeDto>> response = new ApiResponseDto<>(
                "Policy types retrieved successfully",
                HttpStatus.OK.value(),
                policyTypes
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponseDto<List<PolicyTypeDto>>> getActivePolicyTypes() {
        List<PolicyTypeDto> policyTypes = policyTypeService.getActivePolicyTypes();
        ApiResponseDto<List<PolicyTypeDto>> response = new ApiResponseDto<>(
                "Active policy types retrieved successfully",
                HttpStatus.OK.value(),
                policyTypes
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<PolicyTypeDto>> getPolicyTypeById(@PathVariable String id) {
        PolicyTypeDto policyType = policyTypeService.getPolicyTypeById(id);
        if (policyType != null) {
            ApiResponseDto<PolicyTypeDto> response = new ApiResponseDto<>(
                    "Policy type retrieved successfully",
                    HttpStatus.OK.value(),
                    policyType
            );
            return ResponseEntity.ok(response);
        }
        ApiResponseDto<PolicyTypeDto> response = new ApiResponseDto<>(
                "Policy type not found",
                HttpStatus.NOT_FOUND.value(),
                null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponseDto<PolicyTypeDto>> createPolicyType(@RequestBody PolicyTypeRequest policyTypeRequest) {
        PolicyTypeDto createdPolicyType = policyTypeService.createPolicyType(policyTypeRequest);
        ApiResponseDto<PolicyTypeDto> response = new ApiResponseDto<>(
                "Policy type created successfully",
                HttpStatus.CREATED.value(),
                createdPolicyType
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<PolicyTypeDto>> updatePolicyType(@PathVariable String id,
                                                    @RequestBody PolicyTypeRequest policyTypeRequest) {
        PolicyTypeDto updatedPolicyType = policyTypeService.updatePolicyType(id, policyTypeRequest);
        if (updatedPolicyType != null) {
            ApiResponseDto<PolicyTypeDto> response = new ApiResponseDto<>(
                    "Policy type updated successfully",
                    HttpStatus.OK.value(),
                    updatedPolicyType
            );
            return ResponseEntity.ok(response);
        }
        ApiResponseDto<PolicyTypeDto> response = new ApiResponseDto<>(
                "Policy type not found",
                HttpStatus.NOT_FOUND.value(),
                null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<String>> deletePolicyType(@PathVariable String id) {
        boolean deleted = policyTypeService.deletePolicyType(id);
        if (deleted) {
            ApiResponseDto<String> response = new ApiResponseDto<>(
                    "Policy type deleted successfully",
                    HttpStatus.NO_CONTENT.value(),
                    null
            );
            return ResponseEntity.ok(response);
        }
        ApiResponseDto<String> response = new ApiResponseDto<>(
                "Policy type not found",
                HttpStatus.NOT_FOUND.value(),
                null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
}

