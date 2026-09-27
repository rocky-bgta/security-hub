package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.phishing.dto.enums.LandingPageStatus;
import com.aspire.asat.phishing.dto.response.LandingPageDto;
import com.aspire.asat.phishing.mapper.LandingPageMapper;
import com.aspire.asat.phishing.model.Domain;
import com.aspire.asat.phishing.model.LandingPage;
import com.aspire.asat.phishing.repository.DomainRepository;
import com.aspire.asat.phishing.repository.EmailTemplateRepository;
import com.aspire.asat.phishing.repository.LandingPageRepository;
import com.aspire.asat.phishing.service.EmailTemplateLandingPageBindingService;
import com.aspire.asat.phishing.service.HtmlSanitizerService;
import com.aspire.asat.phishing.service.HtmlValidatorService;
import com.aspire.asat.phishing.service.support.CatalogReferenceResolver;
import com.aspire.asat.phishing.service.support.TrackingBaseUrlResolver;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LandingPageServiceImplTrackingDomainTest {

    private static final String CLIENT_ID = "client-1";
    private static final String USER_ID = "user-1";
    private static final String DOMAIN_ID = "domain-1";
    private static final String HOSTNAME = "track.example.com";

    @Mock
    private LandingPageRepository landingPageRepository;
    @Mock
    private EmailTemplateRepository emailTemplateRepository;
    @Mock
    private DomainRepository domainRepository;
    @Mock
    private LandingPageMapper landingPageMapper;
    @Mock
    private CatalogReferenceResolver catalogReferenceResolver;
    @Mock
    private UserCurrentContextService userCurrentContextService;
    @Mock
    private HtmlSanitizerService htmlSanitizerService;
    @Mock
    private HtmlValidatorService htmlValidatorService;
    @Mock
    private TrackingBaseUrlResolver trackingBaseUrlResolver;
    @Mock
    private EmailTemplateLandingPageBindingService bindingService;

    @InjectMocks
    private LandingPageServiceImpl service;

    @BeforeEach
    void stubClientAdminContext() {
        when(userCurrentContextService.getCurrentUserContext())
                .thenReturn(CurrentUserContext.builder()
                        .clientAdminId(CLIENT_ID)
                        .userId(USER_ID)
                        .userType(UserType.CLIENT_ADMIN.getValue())
                        .build());
    }

    @Test
    void getLandingPages_enrichesTrackingDomainHostname() {
        LandingPage page = LandingPage.builder()
                .id("page-1")
                .clientId(CLIENT_ID)
                .createdBy(USER_ID)
                .createdByRole(UserType.CLIENT_ADMIN.getValue())
                .trackingDomainId(DOMAIN_ID)
                .status(LandingPageStatus.ACTIVE)
                .build();
        LandingPageDto mapped = LandingPageDto.builder()
                .pageId("page-1")
                .trackingDomainId(DOMAIN_ID)
                .build();

        when(landingPageRepository.findWithFilters(
                eq(CLIENT_ID), isNull(), isNull(), isNull(), isNull(),
                eq(LandingPageStatus.ACTIVE), isNull(), eq(false), any(Pageable.class)))
                .thenReturn(List.of(page));
        when(landingPageMapper.toListItemDto(eq(page), anyBoolean(), anyBoolean())).thenReturn(mapped);
        when(domainRepository.findAllById(anyCollection()))
                .thenReturn(List.of(Domain.builder().id(DOMAIN_ID).domain(HOSTNAME).build()));

        List<LandingPageDto> result = service.getLandingPages(
                null, null, null, null, LandingPageStatus.ACTIVE, null, null,
                0, 1000, "name", "asc");

        assertEquals(1, result.size());
        assertEquals(HOSTNAME, result.get(0).getTrackingDomain());
        verify(domainRepository).findAllById(anyCollection());
    }

    @Test
    void getLandingPages_missingDomainRow_leavesTrackingDomainNull() {
        LandingPage page = LandingPage.builder()
                .id("page-1")
                .clientId(CLIENT_ID)
                .createdBy(USER_ID)
                .createdByRole(UserType.CLIENT_ADMIN.getValue())
                .trackingDomainId(DOMAIN_ID)
                .build();
        LandingPageDto mapped = LandingPageDto.builder()
                .pageId("page-1")
                .trackingDomainId(DOMAIN_ID)
                .build();

        when(landingPageRepository.findWithFilters(
                eq(CLIENT_ID), isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), eq(false), any(Pageable.class)))
                .thenReturn(List.of(page));
        when(landingPageMapper.toListItemDto(eq(page), anyBoolean(), anyBoolean())).thenReturn(mapped);
        when(domainRepository.findAllById(anyCollection())).thenReturn(List.of());

        List<LandingPageDto> result = service.getLandingPages(
                null, null, null, null, null, null, null, 0, 10, "name", "asc");

        assertNull(result.get(0).getTrackingDomain());
    }

    @Test
    void getLandingPages_blankTrackingDomainId_skipsDomainLookup() {
        LandingPage page = LandingPage.builder()
                .id("page-1")
                .clientId(CLIENT_ID)
                .createdBy(USER_ID)
                .createdByRole(UserType.CLIENT_ADMIN.getValue())
                .build();
        LandingPageDto mapped = LandingPageDto.builder().pageId("page-1").build();

        when(landingPageRepository.findWithFilters(
                eq(CLIENT_ID), isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), eq(false), any(Pageable.class)))
                .thenReturn(List.of(page));
        when(landingPageMapper.toListItemDto(eq(page), anyBoolean(), anyBoolean())).thenReturn(mapped);

        List<LandingPageDto> result = service.getLandingPages(
                null, null, null, null, null, null, null, 0, 10, "name", "asc");

        assertNull(result.get(0).getTrackingDomain());
        verify(domainRepository, never()).findAllById(anyCollection());
    }

    @Test
    void getLandingPageById_enrichesTrackingDomainHostname() {
        LandingPage page = LandingPage.builder()
                .id("page-1")
                .clientId(CLIENT_ID)
                .createdBy(USER_ID)
                .createdByRole(UserType.CLIENT_ADMIN.getValue())
                .trackingDomainId(DOMAIN_ID)
                .build();
        LandingPageDto mapped = LandingPageDto.builder()
                .pageId("page-1")
                .trackingDomainId(DOMAIN_ID)
                .build();

        when(landingPageRepository.findByIdAndClientIdOrGlobal("page-1", CLIENT_ID))
                .thenReturn(Optional.of(page));
        when(landingPageMapper.toDto(eq(page), anyBoolean(), anyBoolean())).thenReturn(mapped);
        when(bindingService.getBoundEmailTemplateIds("page-1", CLIENT_ID)).thenReturn(List.of());
        when(domainRepository.findAllById(anyCollection()))
                .thenReturn(List.of(Domain.builder().id(DOMAIN_ID).domain(HOSTNAME).build()));

        Optional<LandingPageDto> result = service.getLandingPageById("page-1");

        assertEquals(HOSTNAME, result.orElseThrow().getTrackingDomain());
    }
}
