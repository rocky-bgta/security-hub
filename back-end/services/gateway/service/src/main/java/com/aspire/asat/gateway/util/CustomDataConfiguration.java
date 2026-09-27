package com.aspire.asat.gateway.util;

public class CustomDataConfiguration {
    private CustomDataConfiguration() {
    }

    public static final String TOKEN_PREFIX = "Bearer";
    public static final int TOKEN_HEADER_ARRAY_LENGTH = 2;

    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_CURRENT_USER_CONTEXT = "CurrentContext";
    public static final String HEADER_CO_RELATION_ID = "correlationId";

}
