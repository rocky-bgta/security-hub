package com.aspire.asat.cms.controller;

import com.aspire.asat.cms.constant.WebApiUrlConstants;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.userLicence.UserLicenceResponseDto;
import com.aspire.asat.cms.dto.userLicence.BulkUserLicenceRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping(value = WebApiUrlConstants.USER_LICENCE_API, produces = "application/json")
@Tag(name = "User Licence Management", description = "APIs for managing user licences")
public interface UserLicenceController {

    @PostMapping(value = "/bulk", consumes = "application/json")
    @Operation(summary = "Create multiple user licences in bulk", description = "Creates multiple user licences with the provided details in a single operation.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User licences created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    ResponseEntity<ApiResponseDto<List<UserLicenceResponseDto>>> createBulkUserLicences(
            @Valid @RequestBody BulkUserLicenceRequestDto requestDto
    );
}
