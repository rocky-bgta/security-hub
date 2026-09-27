package com.aspire.asat.registration.controller.metadata;

import com.aspire.asat.registration.data.apiResponses.ApiResponseDto;
import com.aspire.asat.registration.data.metadata.request.OrganizationTypeRequestDTO;
import com.aspire.asat.registration.data.metadata.request.OrganizationTypeUpdateRequestDTO;
import com.aspire.asat.registration.data.metadata.response.OrganizationTypeList;
import com.aspire.asat.registration.data.metadata.response.OrganizationTypeResponseDTO;
import com.aspire.asat.registration.service.metadata.OrganizationTypeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class OrganizationTypeControllerImpl implements OrganizationTypeController {

    private final OrganizationTypeService organizationTypeService;

    public OrganizationTypeControllerImpl(OrganizationTypeService organizationTypeService) {
        this.organizationTypeService = organizationTypeService;
    }

    @Override
    public ResponseEntity<ApiResponseDto<OrganizationTypeResponseDTO>> createOrganizationType(OrganizationTypeRequestDTO requestDTO) {
        OrganizationTypeResponseDTO organizationType = organizationTypeService.createOrganizationType(requestDTO);
        ApiResponseDto<OrganizationTypeResponseDTO> response = ApiResponseDto.<OrganizationTypeResponseDTO>builder()
                .data(organizationType)
                .message("Organization type created successfully")
                .statusCode(201)
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<ApiResponseDto<OrganizationTypeResponseDTO>> getOrganizationTypeById(String id) {
        OrganizationTypeResponseDTO organizationType = organizationTypeService.getOrganizationTypeById(id);
        ApiResponseDto<OrganizationTypeResponseDTO> response = ApiResponseDto.<OrganizationTypeResponseDTO>builder()
                .data(organizationType)
                .message("Organization type retrieved successfully")
                .statusCode(HttpStatus.OK.value())
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<OrganizationTypeList>>> getAllOrganizationTypes() {
        List<OrganizationTypeList> organizationTypes = organizationTypeService.getAllOrganizationTypes();
        ApiResponseDto<List<OrganizationTypeList>> response = ApiResponseDto.<List<OrganizationTypeList>>builder()
                .data(organizationTypes)
                .message("Organization types retrieved successfully")
                .statusCode(HttpStatus.OK.value())
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<OrganizationTypeResponseDTO>> updateOrganizationType(OrganizationTypeUpdateRequestDTO requestDTO) {
        OrganizationTypeResponseDTO organizationType = organizationTypeService.updateOrganizationType(requestDTO);
        ApiResponseDto<OrganizationTypeResponseDTO> response = ApiResponseDto.<OrganizationTypeResponseDTO>builder()
                .data(organizationType)
                .message("Organization type updated successfully")
                .statusCode(HttpStatus.OK.value())
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<Void>> deleteOrganizationType(String id) {
        organizationTypeService.deleteOrganizationType(id);
        ApiResponseDto<Void> response = ApiResponseDto.<Void>builder()
                .data(null)
                .message("Organization type deleted successfully")
                .statusCode(HttpStatus.OK.value())
                .build();
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
