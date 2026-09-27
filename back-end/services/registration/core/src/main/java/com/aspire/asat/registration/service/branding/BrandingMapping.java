package com.aspire.asat.registration.service.branding;

import com.aspire.asat.registration.data.branding.response.BrandingResponseDTO;
import com.aspire.asat.registration.model.ClientAdmin;
import com.aspire.asat.registration.model.msp.MspUser;

import java.time.Instant;

/**
 * Maps branding fields ({@code organizationName} → companyName, {@code logoUrl} → logoFilePath)
 * to {@link BrandingResponseDTO}.
 */
public final class BrandingMapping {

    private BrandingMapping() {
    }

    /**
     * Required-info / onboarding: branding considered "present" when a logo URL is stored.
     */
    public static boolean shouldExposeBranding(ClientAdmin ca) {
        return ca != null && ca.getLogoUrl() != null && !ca.getLogoUrl().isBlank();
    }

    public static BrandingResponseDTO toDto(ClientAdmin ca) {
        if (ca == null) {
            return null;
        }
        Instant updated = ca.getUpdatedAt() != null ? ca.getUpdatedAt() : ca.getCreatedAt();
        boolean active = ca.getLogoUrl() != null && !ca.getLogoUrl().isBlank();
        return BrandingResponseDTO.builder()
                .id(ca.getId())
                .companyName(ca.getOrganizationName())
                .logoFilePath(ca.getLogoUrl())
                .clientAdminId(ca.getId())
                .createdAt(ca.getCreatedAt())
                .updatedAt(updated)
                .createdBy(ca.getId())
                .active(active)
                .build();
    }

    public static BrandingResponseDTO toDto(MspUser mspUser) {
        if (mspUser == null) {
            return null;
        }
        Instant updated = mspUser.getUpdatedAt() != null ? mspUser.getUpdatedAt() : mspUser.getCreatedAt();
        boolean active = mspUser.getLogoUrl() != null && !mspUser.getLogoUrl().isBlank();
        return BrandingResponseDTO.builder()
                .id(mspUser.getId())
                .companyName(mspUser.getOrganizationName())
                .logoFilePath(mspUser.getLogoUrl())
                .clientAdminId(mspUser.getId())
                .createdAt(mspUser.getCreatedAt())
                .updatedAt(updated)
                .createdBy(mspUser.getId())
                .active(active)
                .build();
    }
}
