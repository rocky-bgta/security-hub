package com.aspire.asat.cms.service.reports;

import java.util.Collections;
import java.util.List;

/**
 * Client admin scope for CMS client reports (package assignment, topic assignment, etc.).
 */
public record ClientReportScope(String clientAdminId, List<String> clientAdminIds) {

    public static ClientReportScope single(String clientAdminId) {
        return new ClientReportScope(clientAdminId, null);
    }

    public static ClientReportScope multi(List<String> clientAdminIds) {
        return new ClientReportScope(null, clientAdminIds);
    }

    public static ClientReportScope empty() {
        return new ClientReportScope(null, Collections.emptyList());
    }

    public boolean isEmpty() {
        return clientAdminIds != null && clientAdminIds.isEmpty();
    }
}
