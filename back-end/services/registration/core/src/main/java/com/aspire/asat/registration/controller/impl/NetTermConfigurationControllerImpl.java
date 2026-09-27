package com.aspire.asat.registration.controller.impl;

import com.aspire.asat.registration.controller.NetTermConfigurationController;
import com.aspire.asat.registration.data.apiResponses.ApiResponseDto;
import com.aspire.asat.registration.data.apiResponses.AllResponseDto;
import com.aspire.asat.registration.data.netTerm.CreateNetTermConfigurationRequestDTO;
import com.aspire.asat.registration.data.netTerm.NetTermConfigurationDropdownDTO;
import com.aspire.asat.registration.data.netTerm.NetTermConfigurationResponseDTO;
import com.aspire.asat.registration.data.netTerm.UpdateNetTermConfigurationRequestDTO;
import com.aspire.asat.registration.service.NetTermConfigurationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class NetTermConfigurationControllerImpl implements NetTermConfigurationController {

    private final NetTermConfigurationService service;

    @Override
    public ResponseEntity<ApiResponseDto<NetTermConfigurationResponseDTO>> createNetTerm(CreateNetTermConfigurationRequestDTO dto) {
        NetTermConfigurationResponseDTO response = service.createNetTerm(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>("Net term configuration created successfully", 201, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<AllResponseDto<List<NetTermConfigurationResponseDTO>>>> getAllNetTerms(
            String search,
            Boolean isActive,
            int offset,
            int limit
    ) {
        List<NetTermConfigurationResponseDTO> netTerms = service.getAllNetTerms(search, isActive, offset, limit);
        long total = service.getTotalNetTermCount(search, isActive);

        AllResponseDto<List<NetTermConfigurationResponseDTO>> allResponse =
                new AllResponseDto<>(offset, limit, total, netTerms);

        return ResponseEntity.ok(
                new ApiResponseDto<>("Net term configurations fetched successfully", 200, allResponse)
        );
    }

    @Override
    public ResponseEntity<ApiResponseDto<NetTermConfigurationResponseDTO>> getNetTermById(String id) {
        NetTermConfigurationResponseDTO response = service.getNetTermById(id);
        return ResponseEntity.ok(new ApiResponseDto<>("Net term configuration fetched successfully", 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<NetTermConfigurationResponseDTO>> updateNetTerm(
            String id,
            UpdateNetTermConfigurationRequestDTO dto
    ) {
        NetTermConfigurationResponseDTO response = service.updateNetTerm(id, dto);
        return ResponseEntity.ok(new ApiResponseDto<>("Net term configuration updated successfully", 200, response));
    }

    @Override
    public ResponseEntity<ApiResponseDto<Void>> deleteNetTerm(String id) {
        service.deleteNetTerm(id);
        return ResponseEntity.ok(new ApiResponseDto<>("Net term configuration deleted successfully", 200, null));
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<NetTermConfigurationDropdownDTO>>> getAllActiveNetTerms() {
        List<NetTermConfigurationDropdownDTO> netTerms = service.getAllActiveNetTerms();
        return ResponseEntity.ok(
                new ApiResponseDto<>("Active net term configurations retrieved successfully", 200, netTerms)
        );
    }
}

