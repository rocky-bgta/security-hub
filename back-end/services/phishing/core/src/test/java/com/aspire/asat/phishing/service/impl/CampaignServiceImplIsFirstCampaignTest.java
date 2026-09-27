package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.phishing.delivery.CampaignDeliveryOrchestrator;
import com.aspire.asat.phishing.exception.PhishingValidationException;
import com.aspire.asat.phishing.mapper.CampaignMapper;
import com.aspire.asat.phishing.repository.CampaignRecipientRepository;
import com.aspire.asat.phishing.repository.CampaignRepository;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CampaignServiceImplIsFirstCampaignTest {

    private static final String CLIENT_ID = "client-1";
    private static final String PRODUCT_PACKAGE_ID = "pp-1";
    private static final String CAMPAIGN_ID = "cmp-1";

    @Mock private CampaignRepository campaignRepository;
    @Mock private CampaignRecipientRepository recipientRepository;
    @Mock private CampaignMapper campaignMapper;
    @Mock private UserCurrentContextService userCurrentContextService;
    @Mock private CampaignDeliveryOrchestrator campaignDeliveryOrchestrator;

    @InjectMocks
    private CampaignServiceImpl campaignService;

    @BeforeEach
    void setUp() {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(
                CurrentUserContext.builder().clientAdminId(CLIENT_ID).build());
    }

    @Test
    void isFirstCampaign_noRows_returnsTrue() {
        when(campaignRepository.existsByClientIdAndProductPackageId(CLIENT_ID, PRODUCT_PACKAGE_ID))
                .thenReturn(false);

        assertTrue(campaignService.isFirstCampaign(PRODUCT_PACKAGE_ID, null));
        verify(campaignRepository).existsByClientIdAndProductPackageId(CLIENT_ID, PRODUCT_PACKAGE_ID);
    }

    @Test
    void isFirstCampaign_existsForSamePackage_returnsFalse() {
        when(campaignRepository.existsByClientIdAndProductPackageId(CLIENT_ID, PRODUCT_PACKAGE_ID))
                .thenReturn(true);

        assertFalse(campaignService.isFirstCampaign(PRODUCT_PACKAGE_ID, null));
    }

    @Test
    void isFirstCampaign_trimsProductPackageId() {
        when(campaignRepository.existsByClientIdAndProductPackageId(CLIENT_ID, PRODUCT_PACKAGE_ID))
                .thenReturn(false);

        assertTrue(campaignService.isFirstCampaign("  pp-1  ", null));
        verify(campaignRepository).existsByClientIdAndProductPackageId(CLIENT_ID, PRODUCT_PACKAGE_ID);
    }

    @Test
    void isFirstCampaign_blankProductPackageId_throws() {
        org.mockito.Mockito.reset(userCurrentContextService);

        assertThrows(PhishingValidationException.class,
                () -> campaignService.isFirstCampaign("  ", null));
        assertThrows(PhishingValidationException.class,
                () -> campaignService.isFirstCampaign(null, CAMPAIGN_ID));
        verify(campaignRepository, never()).existsByClientIdAndProductPackageId(anyString(), anyString());
        verify(campaignRepository, never())
                .existsByClientIdAndProductPackageIdAndIdNot(anyString(), anyString(), anyString());
    }

    @Test
    void isFirstCampaign_usesClientFromContext() {
        when(campaignRepository.existsByClientIdAndProductPackageId(eq(CLIENT_ID), eq(PRODUCT_PACKAGE_ID)))
                .thenReturn(false);

        campaignService.isFirstCampaign(PRODUCT_PACKAGE_ID, null);

        verify(campaignRepository).existsByClientIdAndProductPackageId(CLIENT_ID, PRODUCT_PACKAGE_ID);
        verify(campaignRepository, never()).existsByClientIdAndProductPackageId(eq("other-client"), anyString());
    }

    @Test
    void isFirstCampaign_withCampaignId_excludesCurrentDraft_returnsTrue() {
        when(campaignRepository.existsByClientIdAndProductPackageIdAndIdNot(
                CLIENT_ID, PRODUCT_PACKAGE_ID, CAMPAIGN_ID))
                .thenReturn(false);

        assertTrue(campaignService.isFirstCampaign(PRODUCT_PACKAGE_ID, CAMPAIGN_ID));
        verify(campaignRepository).existsByClientIdAndProductPackageIdAndIdNot(
                CLIENT_ID, PRODUCT_PACKAGE_ID, CAMPAIGN_ID);
        verify(campaignRepository, never()).existsByClientIdAndProductPackageId(anyString(), anyString());
    }

    @Test
    void isFirstCampaign_withCampaignId_otherCampaignExists_returnsFalse() {
        when(campaignRepository.existsByClientIdAndProductPackageIdAndIdNot(
                CLIENT_ID, PRODUCT_PACKAGE_ID, CAMPAIGN_ID))
                .thenReturn(true);

        assertFalse(campaignService.isFirstCampaign(PRODUCT_PACKAGE_ID, CAMPAIGN_ID));
    }

    @Test
    void isFirstCampaign_withCampaignId_trimsIds() {
        when(campaignRepository.existsByClientIdAndProductPackageIdAndIdNot(
                CLIENT_ID, PRODUCT_PACKAGE_ID, CAMPAIGN_ID))
                .thenReturn(false);

        assertTrue(campaignService.isFirstCampaign("  pp-1  ", "  cmp-1  "));
        verify(campaignRepository).existsByClientIdAndProductPackageIdAndIdNot(
                CLIENT_ID, PRODUCT_PACKAGE_ID, CAMPAIGN_ID);
    }
}
