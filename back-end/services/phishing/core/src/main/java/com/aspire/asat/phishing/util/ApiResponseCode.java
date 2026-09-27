package com.aspire.asat.phishing.util;

import com.aspire.asat.phishing.dto.apiResponses.ApiResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;

@Getter
@AllArgsConstructor
public enum ApiResponseCode {
    OPERATION_SUCCESSFUL(200),

    INVALID_REQUEST_DATA(400),

    UNAUTHORIZED_RESOURCE_ACCESS(401),
    ACCESS_DENY_ERROR(403),
    RECORD_NOT_FOUND(404),
    METHOD_NOT_ALLOWED(405),
    DB_OPERATION_FAILED(422),
    SERVICE_DOMAIN_ERROR(412),

    UNHANDLED_EXCEPTION(500),
    INTER_SERVICE_COMMUNICATION_ERROR(503);

    private final int responseCode;

    public static boolean isOperationSuccessful(ApiResponse<?> apiResponse) {
        return Objects.nonNull(apiResponse) && apiResponse.getStatusCode() == (ApiResponseCode.OPERATION_SUCCESSFUL.getResponseCode());
    }

    public static boolean isNotOperationSuccessful(ApiResponse<?> apiResponse) {
        return !isOperationSuccessful(apiResponse);
    }
}

