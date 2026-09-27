package com.aspire.asat.phishing.utils;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.phishing.exception.ServiceException;

/**
 * Resolves the tenant key used for AI credential storage (SSM + {@code ai_provider_secret_refs}).
 */
public final class AiClientAdminIdSupport {

    private AiClientAdminIdSupport() {
    }

    public static String resolve(CurrentUserContext ctx) {
        if (ctx == null) {
            throw new ServiceException("Current user context is required");
        }
        String clientAdminId = ctx.getClientAdminId();
        if (UserType.CLIENT_ADMIN.name().equals(ctx.getUserType()) || UserType.ASPIRE_ADMIN.name().equals(ctx.getUserType())
                || UserType.SUPER_ADMIN.name().equals(ctx.getUserType()) || UserType.SYSTEM_USER.name().equals(ctx.getUserType())) {
            clientAdminId = ctx.getUserId();
        }
        if (clientAdminId == null || clientAdminId.isBlank()) {
            throw new ServiceException("clientAdminId is required for AI operations");
        }
        return clientAdminId;
    }
}
