package com.aspire.asat.registration.service;

import com.aspire.asat.registration.data.reports.ProductLicenseOnboardingReportDto;

public interface ProductLicenseOnboardingReportService {

    /**
     * Builds the product license user onboarding report for a client admin.
     * When the caller is CLIENT_ADMIN, {@code clientAdminId} may be omitted and the
     * current user id is used; otherwise {@code clientAdminId} is required.
     */
    ProductLicenseOnboardingReportDto getReport(String clientAdminId);
}
