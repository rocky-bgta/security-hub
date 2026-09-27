package com.aspire.asat.universal.client;

import lombok.Getter;

/**
 * Custom exception for HTTP client errors
 */
@Getter
public class HttpClientException extends RuntimeException {

    private final int statusCode;
    private final String responseBody;

    public HttpClientException(String message, int statusCode, String responseBody) {
        super(message);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }

    public HttpClientException(String message, Throwable cause, int statusCode, String responseBody) {
        super(message, cause);
        this.statusCode = statusCode;
        this.responseBody = responseBody;
    }
}
