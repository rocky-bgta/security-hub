package com.aspire.asat.cms.service.reports;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.UserType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Resolves CLIENT_ADMIN / MSP / admin scoping for CMS client reports.
 * Extracted from Package Assignment Report so topic and package reports share one implementation.
 */
@Component
@Slf4j
public class ClientReportScopeResolver {

    public ClientReportScope resolve(CurrentUserContext context, String requestedClientAdminId, String requestedMspId) {
        UserType userType = UserType.fromString(context.getUserType());

        if (UserType.CLIENT_ADMIN.equals(userType)) {
            return ClientReportScope.single(context.getClientAdminId());
        }

        if (isMspReportRequest(context, requestedMspId)) {
            List<String> clientAdminIds = resolveMspClientAdminIds(context, requestedClientAdminId);
            if (clientAdminIds.isEmpty()) {
                return ClientReportScope.empty();
            }
            if (clientAdminIds.size() == 1) {
                return ClientReportScope.single(clientAdminIds.get(0));
            }
            return ClientReportScope.multi(clientAdminIds);
        }

        return ClientReportScope.single(requestedClientAdminId);
    }

    private List<String> resolveMspClientAdminIds(CurrentUserContext context, String clientAdminId) {
        String normalizedClientAdminId = (clientAdminId != null && !clientAdminId.isBlank()) ? clientAdminId.trim() : null;
        if (normalizedClientAdminId != null) {
            return List.of(normalizedClientAdminId);
        }

        List<String> contextClientAdminIds = context.getClientAdminIds();
        if (contextClientAdminIds != null && !contextClientAdminIds.isEmpty()) {
            return contextClientAdminIds;
        }

        log.warn("Unable to resolve client admin IDs for MSP userId={}, userType={}",
                context.getUserId(), context.getUserType());
        return List.of();
    }

    private boolean isMspReportRequest(CurrentUserContext context, String mspId) {
        if (mspId != null && !mspId.isBlank()) {
            return true;
        }
        try {
            return UserType.MSP.equals(UserType.fromString(context.getUserType()));
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
