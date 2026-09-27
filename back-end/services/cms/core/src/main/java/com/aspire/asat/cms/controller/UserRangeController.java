package com.aspire.asat.cms.controller;

import com.aspire.asat.cms.constant.WebApiUrlConstants;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.userRange.UserRangeRequest;
import com.aspire.asat.cms.dto.userRange.UserRangeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RequestMapping(value = WebApiUrlConstants.USER_RANGE_API, produces = "application/json")
@Tag(name = "User Range Management", description = "APIs for managing user ranges")
public interface UserRangeController {

    @PostMapping(consumes = "application/json")
    @Operation(summary = "Create a new user range", description = "Creates a new user range with the provided details.")
    ResponseEntity<ApiResponseDto<UserRangeResponse>> createUserRange(@Valid @RequestBody UserRangeRequest request);

    @GetMapping
    @Operation(summary = "Get all user ranges", description = "Retrieves a list of all active user ranges. This endpoint is publicly accessible.")
    ResponseEntity<ApiResponseDto<List<UserRangeResponse>>> getAllUserRanges(
            @RequestParam(value = "isActive", required = false, defaultValue = "true") Boolean isActive);

    @GetMapping(WebApiUrlConstants.PATH_VAR_ID)
    @Operation(summary = "Get user range by ID", description = "Retrieves a specific user range by its ID.")
    ResponseEntity<ApiResponseDto<UserRangeResponse>> getUserRangeById(@PathVariable String id);

    @PutMapping(WebApiUrlConstants.PATH_VAR_ID)
    @Operation(summary = "Update user range", description = "Updates an existing user range with the provided details.")
    ResponseEntity<ApiResponseDto<UserRangeResponse>> updateUserRange(
            @PathVariable String id,
            @Valid @RequestBody UserRangeRequest request);

    @DeleteMapping(WebApiUrlConstants.PATH_VAR_ID)
    @Operation(summary = "Delete user range", description = "Soft deletes a user range by setting isActive to false.")
    ResponseEntity<ApiResponseDto<String>> deleteUserRange(@PathVariable String id);
}

