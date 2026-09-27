package com.aspire.asat.registration.mapper;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.registration.data.enums.MspStatus;
import com.aspire.asat.registration.data.invoice.InvoiceStatus;
import com.aspire.asat.registration.data.clientAdmin.request.ProductSelectionDto;
import com.aspire.asat.registration.data.mspUser.request.BillingInfoForMspDto;
import com.aspire.asat.registration.data.mspUser.request.MspOnboardingRequestDto;
import com.aspire.asat.registration.data.mspUser.request.OrganizationInfoForMspDto;
import com.aspire.asat.registration.data.mspUser.response.MspOnboardingResponseDto;
import com.aspire.asat.registration.model.msp.MspUser;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Static mapper class for converting between MSP DTOs and entities.
 * This class provides centralized mapping logic for MSP onboarding flow.
 */
@UtilityClass
public class MspUserMapper {

 /**
  * Maps MspOnboardingRequestDto to MspUser entity
  */
 public static MspUser mapToMspUser(MspOnboardingRequestDto requestDto, String adminId, List<String> mspProductIds, CurrentUserContext context, String adminEmail) {
     OrganizationInfoForMspDto org = requestDto.getOrganization();
     BillingInfoForMspDto billing = requestDto.getBilling();

     Instant now = Instant.now();

     MspUser mspUser = new MspUser();
     mspUser.setId(adminId);
     mspUser.setMspId(createMspId());
     mspUser.setOrganizationName(org.getOrganizationName());
     mspUser.setContactEmail(org.getContactEmail());
     mspUser.setMspAdminEmail(adminEmail);
     mspUser.setPhoneNumber(org.getPhoneNumber());
     mspUser.setPhoneCode(org.getPhoneCode());
     mspUser.setCountry(org.getCountry());
     mspUser.setStateProvince(org.getStateProvince());
     mspUser.setTimeZone(org.getTimeZone());
     mspUser.setDomain(org.getDomain());
     mspUser.setMspTypeId(org.getMspTypeId());
     mspUser.setMspTier(org.getTierId());
     mspUser.setOrganizationSize(org.getOrganizationSize());
     mspUser.setOrganizationStreetAddress(org.getStreetAddress());
     mspUser.setOrganizationStreetAddressLine2(org.getStreetAddressLine2());
     mspUser.setOrganizationCity(org.getCity());
     mspUser.setOrganizationStateProvince(org.getStateProvince());
     mspUser.setOrganizationCountry(org.getCountry());
     mspUser.setOrganizationZipPostalCode(org.getZipPostalCode());
     mspUser.setCompanyAddress(buildOrganizationAddress(org));
     mspUser.setLogoUrl(org.getLogoUrl());
     mspUser.setLanguage(org.getLanguage());
     mspUser.setIndustry(org.getIndustry());
     mspUser.setSubIndustry(org.getSubIndustry());
     mspUser.setBillingEmail(billing.getBillingEmail());
     mspUser.setBillingName(billing.getBillingName());
     mspUser.setBillingStreetAddress(billing.getStreetAddress());
     mspUser.setBillingStreetAddressLine2(billing.getStreetAddressLine2());
     mspUser.setBillingCity(billing.getCity());
     mspUser.setBillingStateProvince(billing.getStateProvince());
     mspUser.setBillingCountry(billing.getCountry());
     mspUser.setBillingZipPostalCode(billing.getZipPostalCode());
     mspUser.setBillingAddress(buildBillingAddress(billing, org));
     mspUser.setProductIds(extractProductIds(requestDto.getProductSelections()));
     mspUser.setPackageIds(extractPackageIds(requestDto.getProductSelections()));
     mspUser.setClientProductIds(mspProductIds);
     mspUser.setStatus(MspStatus.PENDING.name());
     mspUser.setCreatedBy(context.getUserId());
     mspUser.setCreatedAt(now);
     mspUser.setUpdatedAt(now);
     mspUser.setInvoiceStatus(InvoiceStatus.PENDING.name());
     mspUser.setDepartment(null); // Department field not available in DTO
     mspUser.setRoleIds(new ArrayList<>());
     mspUser.setNotes("MSP user created during onboarding");

     if (requestDto.getCreditInfo() != null) {
         var credit = requestDto.getCreditInfo();
         if (credit.getEnableCredit() != null && credit.getEnableCredit()) {
             mspUser.setCreditEnabled(true);
             mspUser.setCreditAmount(BigDecimal.valueOf(credit.getCreditAmount()));
             mspUser.setCreditReason(credit.getReason());
             mspUser.setNetDaysId(credit.getNetDaysId());
             mspUser.setAutoSuspendOverDue(credit.getAutoSuspendOnOverdue());
             mspUser.setCreditStartDate(credit.getCreditStartDate());
         }
     }

     return mspUser;
 }
    private static String createMspId() {
        long millis = Instant.now().toEpochMilli();
        long value = Math.abs(millis % 100_000_000L);
        return String.format("MSP-%08d", value);
    }

    /**
     * Maps MspUser entity to MspOnboardingResponseDto
     */
    public static MspOnboardingResponseDto mapToResponseDto(MspUser mspUser, String tempPassword) {
        return MspOnboardingResponseDto.builder()
                .adminId(mspUser.getId())
                .adminEmail(mspUser.getMspAdminEmail())
                .organizationId(UUID.randomUUID().toString()) // Generate new org ID for response
                .organizationName(mspUser.getOrganizationName())
                .tempPassword(tempPassword)
                .portalLink("https://portal.aspireelearning.com")
                .message("MSP onboarding completed successfully")
                .build();
    }

    /**
     * Updates MspUser entity with invoice information
     */
    public static void updateMspUserWithInvoice(MspUser mspUser, String invoiceId, String invoiceStatus) {
        mspUser.setInvoiceId(invoiceId);
        mspUser.setInvoiceStatus(invoiceStatus);
        mspUser.setUpdatedAt(Instant.now());
    }

    /**
     * Updates MspUser entity with credit information
     */
    public static void updateMspUserWithCredit(MspUser mspUser, String creditId, String creditReason) {
        mspUser.setCreditId(creditId);
        mspUser.setCreditEnabled(true);
        mspUser.setCreditReason(creditReason);
        mspUser.setUpdatedAt(Instant.now());
    }

    /**
     * Extracts product IDs from product selections (flat structure)
     */
    private static List<String> extractProductIds(List<ProductSelectionDto> productSelections) {
        List<String> productIds = new ArrayList<>();
        if (productSelections != null) {
            for (ProductSelectionDto selection : productSelections) {
                if (selection.getProductId() != null && !productIds.contains(selection.getProductId())) {
                    productIds.add(selection.getProductId());
                }
            }
        }
        return productIds;
    }

    /**
     * Extracts package IDs from product selections (flat structure)
     */
    private static List<String> extractPackageIds(List<ProductSelectionDto> productSelections) {
        List<String> packageIds = new ArrayList<>();
        if (productSelections != null) {
            for (ProductSelectionDto selection : productSelections) {
                if (selection.getPackageId() != null) {
                    packageIds.add(selection.getPackageId());
                }
            }
        }
        return packageIds;
    }

    /**
     * Builds a formatted organization address string from organization address details
     *
     * @param org The organization information DTO
     * @return Formatted organization address string
     */
    private static String buildOrganizationAddress(OrganizationInfoForMspDto org) {
        StringBuilder addressBuilder = new StringBuilder();

        if (org.getStreetAddress() != null && !org.getStreetAddress().isBlank()) {
            addressBuilder.append(org.getStreetAddress());
        }

        if (org.getStreetAddressLine2() != null && !org.getStreetAddressLine2().isBlank()) {
            if (addressBuilder.length() > 0) {
                addressBuilder.append(", ");
            }
            addressBuilder.append(org.getStreetAddressLine2());
        }

        if (org.getCity() != null && !org.getCity().isBlank()) {
            if (!addressBuilder.isEmpty()) {
                addressBuilder.append(", ");
            }
            addressBuilder.append(org.getCity());
        }

        if (org.getStateProvince() != null && !org.getStateProvince().isBlank()) {
            if (addressBuilder.length() > 0) {
                addressBuilder.append(", ");
            }
            addressBuilder.append(org.getStateProvince());
        }

        if (org.getZipPostalCode() != null && !org.getZipPostalCode().isBlank()) {
            if (addressBuilder.length() > 0) {
                addressBuilder.append(" ");
            }
            addressBuilder.append(org.getZipPostalCode());
        }

        if (org.getCountry() != null && !org.getCountry().isBlank()) {
            if (addressBuilder.length() > 0) {
                addressBuilder.append(", ");
            }
            addressBuilder.append(org.getCountry());
        }

        return addressBuilder.length() > 0 ? addressBuilder.toString() : null;
    }

    /**
     * Builds a formatted billing address string from billing details
     *
     * @param billing The billing information DTO
     * @param org     The organization information DTO (unused, kept for backward compatibility)
     * @return Formatted billing address string
     */
    private static String buildBillingAddress(BillingInfoForMspDto billing, OrganizationInfoForMspDto org) {
        // Build from individual fields
        StringBuilder addressBuilder = new StringBuilder();

        if (billing.getStreetAddress() != null && !billing.getStreetAddress().isBlank()) {
            addressBuilder.append(billing.getStreetAddress());
        }

        if (billing.getStreetAddressLine2() != null && !billing.getStreetAddressLine2().isBlank()) {
            if (addressBuilder.length() > 0) {
                addressBuilder.append(", ");
            }
            addressBuilder.append(billing.getStreetAddressLine2());
        }

        if (billing.getCity() != null && !billing.getCity().isBlank()) {
            if (addressBuilder.length() > 0) {
                addressBuilder.append(", ");
            }
            addressBuilder.append(billing.getCity());
        }

        if (billing.getStateProvince() != null && !billing.getStateProvince().isBlank()) {
            if (addressBuilder.length() > 0) {
                addressBuilder.append(", ");
            }
            addressBuilder.append(billing.getStateProvince());
        }

        if (billing.getZipPostalCode() != null && !billing.getZipPostalCode().isBlank()) {
            if (addressBuilder.length() > 0) {
                addressBuilder.append(" ");
            }
            addressBuilder.append(billing.getZipPostalCode());
        }

        if (billing.getCountry() != null && !billing.getCountry().isBlank()) {
            if (addressBuilder.length() > 0) {
                addressBuilder.append(", ");
            }
            addressBuilder.append(billing.getCountry());
        }

        return addressBuilder.length() > 0 ? addressBuilder.toString() : null;
    }
}
