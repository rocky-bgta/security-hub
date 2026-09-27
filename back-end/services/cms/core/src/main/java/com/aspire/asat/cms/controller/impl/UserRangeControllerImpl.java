package com.aspire.asat.cms.controller.impl;

import com.aspire.asat.cms.controller.UserRangeController;
import com.aspire.asat.cms.dto.apiResponses.ApiResponseDto;
import com.aspire.asat.cms.dto.userRange.UserRangeRequest;
import com.aspire.asat.cms.dto.userRange.UserRangeResponse;
import com.aspire.asat.cms.service.UserRangeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserRangeControllerImpl implements UserRangeController {

    private final UserRangeService userRangeService;

    @Override
    public ResponseEntity<ApiResponseDto<UserRangeResponse>> createUserRange(@Valid UserRangeRequest request) {
        UserRangeResponse response = userRangeService.createUserRange(request);
        ApiResponseDto<UserRangeResponse> apiResponse = new ApiResponseDto<>(
                "User range created successfully", HttpStatus.CREATED.value(), response);
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<ApiResponseDto<List<UserRangeResponse>>> getAllUserRanges(Boolean isActive) {
        List<UserRangeResponse> response = userRangeService.getAllUserRanges(isActive);
        ApiResponseDto<List<UserRangeResponse>> apiResponse = new ApiResponseDto<>(
                "User ranges retrieved successfully", HttpStatus.OK.value(), response);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<UserRangeResponse>> getUserRangeById(String id) {
        UserRangeResponse response = userRangeService.getUserRangeById(id);
        ApiResponseDto<UserRangeResponse> apiResponse = new ApiResponseDto<>(
                "User range retrieved successfully", HttpStatus.OK.value(), response);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<UserRangeResponse>> updateUserRange(String id, @Valid UserRangeRequest request) {
        UserRangeResponse response = userRangeService.updateUserRange(id, request);
        ApiResponseDto<UserRangeResponse> apiResponse = new ApiResponseDto<>(
                "User range updated successfully", HttpStatus.OK.value(), response);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ApiResponseDto<String>> deleteUserRange(String id) {
        userRangeService.deleteUserRange(id);
        ApiResponseDto<String> apiResponse = new ApiResponseDto<>(
                "User range deleted successfully", HttpStatus.OK.value(), "Deleted user range with id: " + id);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }
}

