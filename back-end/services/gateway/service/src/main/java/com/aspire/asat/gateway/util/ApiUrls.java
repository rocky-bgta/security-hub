package com.aspire.asat.gateway.util;

import java.util.List;

public final class ApiUrls {
    private ApiUrls() {
    }

    //TODO: will move the public URLS into database later for better management
    public static List<String> getPublicUrls() {
        return List.of(
                "/auth/api/v1/auth/login",
                "/auth/api/v1/auth/health"
        );
    }

    public static List<String> getCommonPublicUrls() {
        return List.of(
                "/**/v3/api-docs/**",
                "/**/swagger-ui/**",
                "/**/swagger-ui.html",
                "/**/webjars/**",
                "/actuator/**"
        );
    }


}
