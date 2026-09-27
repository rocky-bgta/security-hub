package com.aspire.asat.registration.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.registration.data.department.request.DepartmentCreateRequestDTO;
import com.aspire.asat.registration.data.department.request.DepartmentUpdateRequestDTO;
import com.aspire.asat.registration.data.department.response.DepartmentResponseDTO;
import com.aspire.asat.registration.data.department.response.DepartmentUserCountResponseDTO;
import com.aspire.asat.registration.data.enums.RiskGroup;
import com.aspire.asat.registration.data.riskgroup.response.RiskGroupUserCountResponseDTO;
import com.aspire.asat.registration.exception.ResourceNotFoundException;
import com.aspire.asat.registration.model.Department;
import com.aspire.asat.registration.repository.AspireUserRepository;
import com.aspire.asat.registration.repository.ClientAdminRepository;
import com.aspire.asat.registration.repository.DepartmentRepository;
import com.aspire.asat.registration.service.DepartmentService;
import com.aspire.asat.registration.utils.UserCurrentContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final ClientAdminRepository clientAdminRepository;
    private final AspireUserRepository aspireUserRepository;
    private final UserCurrentContextService userCurrentContextService;

    @Override
    public void findOrCreateDepartment(String departmentName, String clientAdminId) {
        if (!StringUtils.hasText(departmentName)) {
            throw new IllegalArgumentException("Department name cannot be null or empty");
        }

        log.info("Finding or creating department: {} for client admin: {}", departmentName, clientAdminId);

        // First try to find existing department
        Optional<Department> existingDepartment = departmentRepository.findByNameIgnoreCase(departmentName);

        if (existingDepartment.isPresent()) {
            log.info("Found existing department: {} for client admin: {}", departmentName, clientAdminId);
            return;
        }

        // Create new department if it doesn't exist
        log.info("Creating new department: {} for client admin: {}", departmentName, clientAdminId);
        createDepartment(departmentName, Optional.ofNullable(clientAdminId), false);
    }

    @Override
    public void createDepartment(String name, Optional<String> clientAdminId, Boolean isSystemDefined) {
        CurrentUserContext userContext = userCurrentContextService.getCurrentUserContext();
        Department department = Department.builder()
                .name(name)
                .description("Department for " + name)
                .clientAdminId(clientAdminId.orElse(null))
                .isSystemDefined(isSystemDefined != null && isSystemDefined)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .createdBy(userContext.getUserId())
                .active(true)
                .build();
        Department savedDepartment = departmentRepository.save(department);
        log.info("Created department: {} with ID: {} for client admin: {}", name, savedDepartment.getId(), clientAdminId);
    }

    @Override
    public DepartmentResponseDTO createDepartment(DepartmentCreateRequestDTO requestDTO) {
        log.info("Creating department: {}", requestDTO.getName());

        // Check if department with same name already exists
        Optional<Department> existingDepartment = departmentRepository.findByNameIgnoreCase(requestDTO.getName());
        if (existingDepartment.isPresent()) {
            throw new IllegalArgumentException("Department with name '" + requestDTO.getName() + "' already exists");
        }

        CurrentUserContext userContext = userCurrentContextService.getCurrentUserContext();
        Department department = Department.builder()
                .name(requestDTO.getName())
                .description(requestDTO.getDescription())
                .clientAdminId(null)
                .isSystemDefined(requestDTO.getIsSystemDefined() != null ? requestDTO.getIsSystemDefined() : true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .createdBy(userContext.getUserId())
                .active(true)
                .build();

        Department savedDepartment = departmentRepository.save(department);
        log.info("Created department: {} with ID: {}", requestDTO.getName(), savedDepartment.getId());

        return convertToResponseDTO(savedDepartment);
    }

    @Override
    public List<DepartmentResponseDTO> getAllDepartments(String search, String clientAdminId, Boolean active, 
                                                        Boolean isSystemDefined, int offset, int pageSize, 
                                                        String sortBy, String sortDirection) {
        log.info("Getting departments with filters - search: {}, clientAdminId: {}, active: {}, isSystemDefined: {}, offset: {}, pageSize: {}",
                search, clientAdminId, active, isSystemDefined, offset, pageSize);

        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(offset, pageSize, sort);

        Page<Department> departmentPage;

        if (StringUtils.hasText(clientAdminId)) {
            // Fetch departments for the client admin PLUS all system-defined departments
            departmentPage = departmentRepository.findDepartmentsForClientAdminWithSystemDefined(
                    search != null ? search : "", active, clientAdminId, pageable);
        } else {
            departmentPage = departmentRepository.findDepartmentsWithFilters(
                    search != null ? search : "", active, isSystemDefined, clientAdminId, pageable);
        }

        return departmentPage.getContent().stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    @Override
    public long countDepartments(String search, String clientAdminId, Boolean active, Boolean isSystemDefined) {
        log.info("Counting departments with filters - search: {}, clientAdminId: {}, active: {}, isSystemDefined: {}", 
                search, clientAdminId, active, isSystemDefined);

        return departmentRepository.countDepartmentsWithFilters(
                search != null ? search : "", active, isSystemDefined, clientAdminId);
    }

    @Override
    public DepartmentResponseDTO updateDepartment(DepartmentUpdateRequestDTO requestDTO) {
        log.info("Updating department: {}", requestDTO.getId());

        Department existingDepartment = departmentRepository.findById(requestDTO.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + requestDTO.getId()));

        // Check if another department with same name exists (excluding current one)
        Optional<Department> duplicateDepartment = departmentRepository.findByNameIgnoreCase(requestDTO.getName());
        if (duplicateDepartment.isPresent() && !duplicateDepartment.get().getId().equals(requestDTO.getId())) {
            throw new IllegalArgumentException("Department with name '" + requestDTO.getName() + "' already exists");
        }

        // Update fields
        existingDepartment.setName(requestDTO.getName());
        existingDepartment.setDescription(requestDTO.getDescription());
        if (requestDTO.getIsSystemDefined() != null) {
            existingDepartment.setIsSystemDefined(requestDTO.getIsSystemDefined());
        }
        if(requestDTO.getActive() != null) {
            existingDepartment.setActive(requestDTO.getActive());
        }
        existingDepartment.setUpdatedAt(Instant.now());

        Department updatedDepartment = departmentRepository.save(existingDepartment);
        log.info("Updated department: {} with ID: {}", requestDTO.getName(), updatedDepartment.getId());

        return convertToResponseDTO(updatedDepartment);
    }

    @Override
    public void deleteDepartment(String departmentId) {
        log.info("Soft deleting department: {}", departmentId);

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + departmentId));

        // Soft delete by setting active to false
        department.setActive(false);
        department.setUpdatedAt(Instant.now());

        departmentRepository.save(department);
        log.info("Soft deleted department: {} with ID: {}", department.getName(), departmentId);
    }

    @Override
    public Optional<DepartmentResponseDTO> getDepartmentById(String departmentId) {
        log.info("Getting department by ID: {}", departmentId);

        return departmentRepository.findById(departmentId)
                .map(this::convertToResponseDTO);
    }

    /**
     * Department names and counts come only from {@link AspireUser} (collection {@code aspire_user}),
     * matched by {@code clientAdminId} and end-user rows ({@code userType = USER}), grouped by the
     * user's {@code department} field.
     */
    @Override
    public List<DepartmentUserCountResponseDTO> getDepartmentUserCounts(String clientAdminId) {
        if (!StringUtils.hasText(clientAdminId)) {
            throw new IllegalArgumentException("clientAdminId is required");
        }
        if (!clientAdminRepository.existsById(clientAdminId)) {
            throw new ResourceNotFoundException("Client admin not found with ID: " + clientAdminId);
        }

        Map<String, Long> merged = aspireUserRepository.countUsersGroupedByDepartment(
                clientAdminId, UserType.USER.getValue());

        List<DepartmentUserCountResponseDTO> list = new ArrayList<>();
        for (Map.Entry<String, Long> e : merged.entrySet()) {
            list.add(DepartmentUserCountResponseDTO.builder()
                    .departmentName(e.getKey())
                    .userCount(e.getValue())
                    .build());
        }
        list.sort(Comparator.comparing(DepartmentUserCountResponseDTO::getDepartmentName,
                String.CASE_INSENSITIVE_ORDER));
        return list;
    }

    @Override
    public List<RiskGroupUserCountResponseDTO> getRiskGroupUserCounts(String clientAdminId) {
        if (!StringUtils.hasText(clientAdminId)) {
            throw new IllegalArgumentException("clientAdminId is required");
        }
        if (!clientAdminRepository.existsById(clientAdminId)) {
            throw new ResourceNotFoundException("Client admin not found with ID: " + clientAdminId);
        }

        Map<String, Long> merged = aspireUserRepository.countUsersGroupedByRiskGroup(
                clientAdminId, UserType.USER.getValue());

        List<RiskGroupUserCountResponseDTO> list = new ArrayList<>();
        for (Map.Entry<String, Long> e : merged.entrySet()) {
            RiskGroup rg = parseRiskGroupKey(e.getKey());
            list.add(RiskGroupUserCountResponseDTO.builder()
                    .riskGroup(rg)
                    .userCount(e.getValue())
                    .build());
        }
        list.sort(Comparator.comparing(RiskGroupUserCountResponseDTO::getRiskGroup,
                Comparator.nullsLast(Comparator.naturalOrder())));
        return list;
    }

    private static RiskGroup parseRiskGroupKey(String key) {
        if ("UNASSIGNED".equals(key)) {
            return null;
        }
        return RiskGroup.valueOf(key);
    }

    private DepartmentResponseDTO convertToResponseDTO(Department department) {
        return DepartmentResponseDTO.builder()
                .id(department.getId())
                .name(department.getName())
                .description(department.getDescription())
                .clientAdminId(department.getClientAdminId())
                .isSystemDefined(department.getIsSystemDefined())
                .createdAt(department.getCreatedAt())
                .updatedAt(department.getUpdatedAt())
                .createdBy(department.getCreatedBy())
                .active(department.isActive())
                .build();
    }
}
