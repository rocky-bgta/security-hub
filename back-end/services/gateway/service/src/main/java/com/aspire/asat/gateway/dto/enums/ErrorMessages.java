package com.aspire.asat.gateway.dto.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorMessages {

    AUTH_HEADER_MISSING("GE4001", "You credentials are messing. Please try again.", HttpStatus.UNAUTHORIZED),
    AUTH_HEADER_MISS_MATCH("GE400", "You credentials are miss match. Please try again.", HttpStatus.BAD_REQUEST),
    INVALID_AUTH_TOKEN("GE4001", "You credentials are invalid. Please sign in again.", HttpStatus.UNAUTHORIZED),

    SESSION_TIMEOUT_IN_REDIS("GE1001", "Your session timeout in redis.", HttpStatus.FORBIDDEN),

    AUTH_TOKEN_EXPIRED("GE401", "Your Token is expired, Please Login Again", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED_ACCESS("GE401", "You are not allowed to access this resource", HttpStatus.UNAUTHORIZED),

    ;

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
