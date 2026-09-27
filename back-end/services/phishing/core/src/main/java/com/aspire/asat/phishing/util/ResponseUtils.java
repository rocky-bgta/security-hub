package com.aspire.asat.phishing.util;

import com.aspire.asat.phishing.dto.apiResponses.ApiResponse;
import org.springframework.http.ResponseEntity;

public class ResponseUtils {

    public static <T> ResponseEntity<ApiResponse<T>> createSuccessResponseObject(String message) {
        ApiResponse<T> apiResponse = new ApiResponse<T>();
        apiResponse.setStatusCode(ApiResponseCode.OPERATION_SUCCESSFUL.getResponseCode());
        apiResponse.setMessage(message);
        return ResponseEntity.ok(apiResponse);
    }

    public static <T> ResponseEntity<ApiResponse<T>> createSuccessResponseObject(String message, T data) {
        ApiResponse<T> apiResponse = new ApiResponse<T>();
        apiResponse.setStatusCode(ApiResponseCode.OPERATION_SUCCESSFUL.getResponseCode());
        apiResponse.setMessage(message);
        apiResponse.setData(data);
        return ResponseEntity.ok(apiResponse);
    }

    public static <T> ApiResponse<T> createApiResponse(int responseCode, String responseMessage, T data) {
        ApiResponse<T> apiResponse = new ApiResponse<T>();
        apiResponse.setStatusCode(responseCode);
        apiResponse.setMessage(responseMessage);
        apiResponse.setData(data);
        return apiResponse;
    }

    public static <T> ApiResponse<T> createApiResponse(int responseCode, String responseMessage) {
        ApiResponse<T> apiResponse = new ApiResponse<T>();
        apiResponse.setStatusCode(responseCode);
        apiResponse.setMessage(responseMessage);
        return apiResponse;
    }
}

