package com.aspire.asat.registration.controller;

import com.aspire.asat.registration.constant.WebApiUrlConstants;
import com.aspire.asat.registration.data.apiResponses.AllResponseDto;
import com.aspire.asat.registration.data.apiResponses.ApiResponseDto;
import com.aspire.asat.registration.data.department.request.DepartmentCreateRequestDTO;
import com.aspire.asat.registration.data.department.request.DepartmentUpdateRequestDTO;
import com.aspire.asat.registration.data.department.response.DepartmentResponseDTO;
import com.aspire.asat.registration.data.department.response.DepartmentUserCountResponseDTO;
import com.aspire.asat.registration.data.riskgroup.response.RiskGroupUserCountResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "Department Management", description = "APIs for managing departments (create, list, update, delete)")
@RequestMapping(value = WebApiUrlConstants.DEPARTMENTS, produces = "application/json")
public interface DepartmentController {

    @PostMapping()
    ResponseEntity<ApiResponseDto<DepartmentResponseDTO>> createDepartment(
            @RequestBody DepartmentCreateRequestDTO requestDTO);

    @GetMapping()
    ResponseEntity<ApiResponseDto<AllResponseDto<List<DepartmentResponseDTO>>>> getAllDepartments(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String clientAdminId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Boolean isSystemDefined,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDirection);

    @GetMapping("/{departmentId}")
    ResponseEntity<ApiResponseDto<DepartmentResponseDTO>> getDepartmentById(
            @PathVariable String departmentId);

    @PutMapping()
    ResponseEntity<ApiResponseDto<DepartmentResponseDTO>> updateDepartment(
            @RequestBody DepartmentUpdateRequestDTO requestDTO);

    @DeleteMapping("/{departmentId}")
    ResponseEntity<ApiResponseDto<Void>> deleteDepartment(
            @PathVariable String departmentId);

    @Operation(summary = "Department user counts by client admin",
            description = "Returns each department name with the count of users (userType USER) for the given client admin. "
                    + "clientAdminId can be supplied as a path variable or query parameter.")
    @GetMapping("/client-admin/{clientAdminId}/user-counts")
    ResponseEntity<ApiResponseDto<List<DepartmentUserCountResponseDTO>>> getDepartmentUserCountsByClientAdminPath(
            @PathVariable String clientAdminId);

    @GetMapping("/user-counts")
    ResponseEntity<ApiResponseDto<List<DepartmentUserCountResponseDTO>>> getDepartmentUserCountsByClientAdminParam(
            @RequestParam String clientAdminId);

    @Operation(summary = "Risk group user counts by client admin",
            description = "Returns each risk group with the count of users (userType USER) for the given client admin. "
                    + "Pass clientAdminId as a query parameter.")
    @GetMapping("/risk-group-user-counts")
    ResponseEntity<ApiResponseDto<List<RiskGroupUserCountResponseDTO>>> getRiskGroupUserCountsByClientAdminParam(
            @RequestParam String clientAdminId);

}
