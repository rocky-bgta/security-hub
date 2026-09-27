package com.aspire.asat.registration.controller.impl;

import com.aspire.asat.registration.controller.DepartmentController;
import com.aspire.asat.registration.data.apiResponses.AllResponseDto;
import com.aspire.asat.registration.data.apiResponses.ApiResponseDto;
import com.aspire.asat.registration.data.department.request.DepartmentCreateRequestDTO;
import com.aspire.asat.registration.data.department.request.DepartmentUpdateRequestDTO;
import com.aspire.asat.registration.data.department.response.DepartmentResponseDTO;
import com.aspire.asat.registration.data.department.response.DepartmentUserCountResponseDTO;
import com.aspire.asat.registration.data.riskgroup.response.RiskGroupUserCountResponseDTO;
import com.aspire.asat.registration.exception.ResourceNotFoundException;
import com.aspire.asat.registration.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class DepartmentControllerImpl implements DepartmentController {

    private final DepartmentService departmentService;

    @Override
    public ResponseEntity<ApiResponseDto<DepartmentResponseDTO>> createDepartment(DepartmentCreateRequestDTO requestDTO) {
        try {
            log.info("Creating department: {}", requestDTO.getName());
            DepartmentResponseDTO response = departmentService.createDepartment(requestDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponseDto<>("Department created successfully", 201, response));
        } catch (IllegalArgumentException e) {
            log.error("Invalid request for creating department: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Error creating department: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to create department", 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<DepartmentResponseDTO>>>> getAllDepartments(
            String search, String clientAdminId, Boolean active, Boolean isSystemDefined,
            int offset, int pageSize, String sortBy, String sortDirection) {
        
        try {
            log.info("Getting departments with filters - search: {}, clientAdminId: {}, active: {}, isSystemDefined: {}", 
                    search, clientAdminId, active, isSystemDefined);

            List<DepartmentResponseDTO> departments = departmentService.getAllDepartments(
                    search, clientAdminId, active, isSystemDefined, offset, pageSize, sortBy, sortDirection);
            
            long total = departmentService.countDepartments(search, clientAdminId, active, isSystemDefined);

            AllResponseDto<List<DepartmentResponseDTO>> response = new AllResponseDto<>(offset, pageSize, total, departments);
            return ResponseEntity.ok(new ApiResponseDto<>("Departments retrieved successfully", 200, response));
        } catch (Exception e) {
            log.error("Error getting departments: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve departments", 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<DepartmentResponseDTO>> getDepartmentById(String departmentId) {
        try {
            log.info("Getting department by ID: {}", departmentId);
            return departmentService.getDepartmentById(departmentId)
                    .map(department -> ResponseEntity.ok(new ApiResponseDto<>("Department retrieved successfully", 200, department)))
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(new ApiResponseDto<>("Department not found", 404, null)));
        } catch (Exception e) {
            log.error("Error getting department by ID: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve department", 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<DepartmentResponseDTO>> updateDepartment(DepartmentUpdateRequestDTO requestDTO) {
        try {
            log.info("Updating department: {}", requestDTO.getId());
            DepartmentResponseDTO response = departmentService.updateDepartment(requestDTO);
            return ResponseEntity.ok(new ApiResponseDto<>("Department updated successfully", 200, response));
        } catch (IllegalArgumentException e) {
            log.error("Invalid request for updating department: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (Exception e) {
            log.error("Error updating department: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to update department", 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<Void>> deleteDepartment(String departmentId) {
        try {
            log.info("Deleting department: {}", departmentId);
            departmentService.deleteDepartment(departmentId);
            return ResponseEntity.ok(new ApiResponseDto<>("Department deleted successfully", 200, null));
        } catch (Exception e) {
            log.error("Error deleting department: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to delete department", 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<DepartmentUserCountResponseDTO>>> getDepartmentUserCountsByClientAdminPath(
            String clientAdminId) {
        return getDepartmentUserCountsByClientAdmin(clientAdminId);
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<DepartmentUserCountResponseDTO>>> getDepartmentUserCountsByClientAdminParam(
            String clientAdminId) {
        return getDepartmentUserCountsByClientAdmin(clientAdminId);
    }

    private ResponseEntity<ApiResponseDto<List<DepartmentUserCountResponseDTO>>> getDepartmentUserCountsByClientAdmin(
            String clientAdminId) {
        try {
            List<DepartmentUserCountResponseDTO> counts = departmentService.getDepartmentUserCounts(clientAdminId);
            return ResponseEntity.ok(new ApiResponseDto<>("Department user counts retrieved successfully", 200, counts));
        } catch (IllegalArgumentException e) {
            log.error("Invalid request for department user counts: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (ResourceNotFoundException e) {
            log.error("Client admin not found for department user counts: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDto<>(e.getMessage(), 404, null));
        } catch (Exception e) {
            log.error("Error retrieving department user counts: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve department user counts", 500, null));
        }
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<RiskGroupUserCountResponseDTO>>> getRiskGroupUserCountsByClientAdminParam(
            String clientAdminId) {
        return getRiskGroupUserCountsByClientAdmin(clientAdminId);
    }

    private ResponseEntity<ApiResponseDto<List<RiskGroupUserCountResponseDTO>>> getRiskGroupUserCountsByClientAdmin(
            String clientAdminId) {
        try {
            List<RiskGroupUserCountResponseDTO> counts = departmentService.getRiskGroupUserCounts(clientAdminId);
            return ResponseEntity.ok(new ApiResponseDto<>("Risk group user counts retrieved successfully", 200, counts));
        } catch (IllegalArgumentException e) {
            log.error("Invalid request for risk group user counts: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponseDto<>(e.getMessage(), 400, null));
        } catch (ResourceNotFoundException e) {
            log.error("Client admin not found for risk group user counts: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponseDto<>(e.getMessage(), 404, null));
        } catch (Exception e) {
            log.error("Error retrieving risk group user counts: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>("Failed to retrieve risk group user counts", 500, null));
        }
    }
}
