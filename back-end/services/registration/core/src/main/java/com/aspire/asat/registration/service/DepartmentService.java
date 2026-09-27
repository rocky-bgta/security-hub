package com.aspire.asat.registration.service;

import com.aspire.asat.registration.data.department.request.DepartmentCreateRequestDTO;
import com.aspire.asat.registration.data.department.request.DepartmentUpdateRequestDTO;
import com.aspire.asat.registration.data.department.response.DepartmentResponseDTO;
import com.aspire.asat.registration.data.department.response.DepartmentUserCountResponseDTO;
import com.aspire.asat.registration.data.riskgroup.response.RiskGroupUserCountResponseDTO;

import java.util.List;
import java.util.Optional;

public interface DepartmentService {

    /**
     * Find or create a department by name for a specific client admin
     */
    void findOrCreateDepartment(String departmentName, String clientAdminId);

    /**
     * Create a new department
     */
    void createDepartment(String name, Optional<String> clientAdminId, Boolean isSystemDefined);

    /**
     * Create department from DTO
     */
    DepartmentResponseDTO createDepartment(DepartmentCreateRequestDTO requestDTO);

    /**
     * Get all departments with advanced filtering and pagination
     */
    List<DepartmentResponseDTO> getAllDepartments(String search, String clientAdminId, Boolean active, 
                                                Boolean isSystemDefined, int offset, int pageSize, 
                                                String sortBy, String sortDirection);

    /**
     * Count departments with filters
     */
    long countDepartments(String search, String clientAdminId, Boolean active, Boolean isSystemDefined);

    /**
     * Update department
     */
    DepartmentResponseDTO updateDepartment(DepartmentUpdateRequestDTO requestDTO);

    /**
     * Soft delete department by setting active to false
     */
    void deleteDepartment(String departmentId);

    /**
     * Get department by ID
     */
    Optional<DepartmentResponseDTO> getDepartmentById(String departmentId);

    /**
     * Returns each department name with the number of end users ({@code userType = USER}) for the given client admin.
     */
    List<DepartmentUserCountResponseDTO> getDepartmentUserCounts(String clientAdminId);

    /**
     * Returns each risk group with the number of end users ({@code userType = USER}) for the given client admin.
     */
    List<RiskGroupUserCountResponseDTO> getRiskGroupUserCounts(String clientAdminId);
}
