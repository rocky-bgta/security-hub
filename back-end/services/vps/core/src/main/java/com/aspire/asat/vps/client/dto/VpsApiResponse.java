package com.aspire.asat.vps.client.dto;

import lombok.Data;

@Data
public class VpsApiResponse<T> {
    private String message;
    private int statusCode;
    private T data;

    public VpsApiResponse(String message, int statusCode, T data) {
        this.message = message;
        this.statusCode = statusCode;
        this.data = data;
    }
}
