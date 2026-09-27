package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.phishing.dto.enums.ProfileType;
import com.aspire.asat.phishing.dto.request.SenderProfileRequest;
import com.aspire.asat.phishing.dto.response.SenderProfileDto;
import com.aspire.asat.phishing.dto.response.SenderProfileImportResultDto;
import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.mapper.SenderProfileMapper;
import com.aspire.asat.phishing.model.SenderProfile;
import com.aspire.asat.phishing.repository.CampaignRepository;
import com.aspire.asat.phishing.repository.DomainRepository;
import com.aspire.asat.phishing.repository.SenderProfileRepository;
import com.aspire.asat.phishing.service.SmtpTestService;
import com.aspire.asat.phishing.service.support.CatalogReferenceResolver;
import com.aspire.asat.phishing.service.support.CredentialEncryptionService;
import com.aspire.asat.phishing.utils.UserCurrentContextService;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SenderProfileServiceImplTest {

    @Mock
    private SenderProfileRepository senderProfileRepository;
    @Mock
    private CampaignRepository campaignRepository;
    @Mock
    private DomainRepository domainRepository;
    @Mock
    private SenderProfileMapper senderProfileMapper;
    @Mock
    private SmtpTestService smtpTestService;
    @Mock
    private UserCurrentContextService userCurrentContextService;
    @Mock
    private CatalogReferenceResolver catalogReferenceResolver;
    @Mock
    private Validator validator;
    @Mock
    private CredentialEncryptionService credentialEncryptionService;

    @InjectMocks
    private SenderProfileServiceImpl senderProfileService;

    @Test
    void importSenderProfilesShouldImportValidRowsAndReturnSummary() throws Exception {
        String csv = "profileName,interfaceType,fromAddress,host,port,username,password,providerType\n"
                + "Finance SMTP,SMTP,alerts@example.com,smtp.example.com,587,user1,pass1,OTHER\n";
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("profiles.csv");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)));

        CurrentUserContext context = CurrentUserContext.builder()
                .clientAdminId("client-1")
                .userId("user-1")
                .userType(UserType.CLIENT_ADMIN.getValue())
                .build();

        SenderProfile entity = SenderProfile.builder()
                .id("sp-1")
                .profileName("Finance SMTP")
                .fromAddress("alerts@example.com")
                .host("smtp.example.com")
                .port(587)
                .username("user1")
                .password("encoded")
                .build();
        SenderProfileDto dto = SenderProfileDto.builder()
                .profileId("sp-1")
                .profileName("Finance SMTP")
                .build();

        when(userCurrentContextService.getCurrentUserContext()).thenReturn(context);
        when(validator.validate(any(SenderProfileRequest.class))).thenReturn(Collections.emptySet());
        when(senderProfileRepository.existsByClientIdAndProfileName(eq("client-1"), anyString())).thenReturn(false);
        when(domainRepository.findByClientIdAndDomainName(anyString(), anyString())).thenReturn(Optional.empty());
        when(senderProfileMapper.toEntity(any(SenderProfileRequest.class), eq("client-1"))).thenReturn(entity);
        when(catalogReferenceResolver.resolveDeceptionLevel(any())).thenReturn(null);
        when(catalogReferenceResolver.resolvePersonalizationLevel(any())).thenReturn(null);
        when(credentialEncryptionService.encrypt(anyString())).thenAnswer(inv -> "enc-" + inv.getArgument(0));
        when(senderProfileRepository.save(any(SenderProfile.class))).thenReturn(entity);
        when(senderProfileMapper.toDto(any(SenderProfile.class), eq(true), eq(true))).thenReturn(dto);

        SenderProfileImportResultDto result = senderProfileService.importSenderProfiles(file);

        assertEquals(1, result.getTotalRows());
        assertEquals(1, result.getSuccessCount());
        assertEquals(0, result.getFailedCount());
    }

    @Test
    void importSenderProfilesShouldFailWhenRequiredHeadersMissing() throws Exception {
        String csv = "profileName,interfaceType,fromAddress,port\n"
                + "Finance SMTP,SMTP,alerts@example.com,587\n";
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("profiles.csv");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)));

        ServiceException exception = assertThrows(
                ServiceException.class, () -> senderProfileService.importSenderProfiles(file));

        assertEquals("Missing required CSV header: host", exception.getMessage());
    }

    @Test
    void getSenderProfiles_aspireAdminCanEditAndDeleteManagedProfiles() {
        CurrentUserContext context = CurrentUserContext.builder()
                .clientAdminId("client-1")
                .userId("admin-1")
                .userType(UserType.ASPIRE_ADMIN.getValue())
                .build();
        SenderProfile managed = SenderProfile.builder()
                .id("sp-managed")
                .profileName("Managed Profile")
                .profileType(ProfileType.MANAGED)
                .isGlobal(true)
                .createdBy("other-admin")
                .createdByRole(UserType.SUPER_ADMIN.getValue())
                .build();
        SenderProfileDto dto = SenderProfileDto.builder().profileId("sp-managed").build();

        when(userCurrentContextService.getCurrentUserContext()).thenReturn(context);
        when(senderProfileRepository.findWithFilters(
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(List.of(managed));
        when(senderProfileMapper.toDto(eq(managed), eq(true), eq(true))).thenReturn(dto);

        List<SenderProfileDto> result = senderProfileService.getSenderProfiles(
                0, 10, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                "createdAt", "desc");

        assertEquals(1, result.size());
        verify(senderProfileMapper).toDto(managed, true, true);
    }

    @Test
    void getSenderProfiles_clientAdminOnlyEditsOwnCustomProfiles() {
        CurrentUserContext context = CurrentUserContext.builder()
                .clientAdminId("client-1")
                .userId("user-1")
                .userType(UserType.CLIENT_ADMIN.getValue())
                .build();
        SenderProfile own = SenderProfile.builder()
                .id("sp-own")
                .profileType(ProfileType.CUSTOM)
                .isGlobal(false)
                .createdBy("user-1")
                .createdByRole(UserType.CLIENT_ADMIN.getValue())
                .build();
        SenderProfile other = SenderProfile.builder()
                .id("sp-other")
                .profileType(ProfileType.CUSTOM)
                .isGlobal(false)
                .createdBy("user-2")
                .createdByRole(UserType.CLIENT_ADMIN.getValue())
                .build();
        SenderProfile managed = SenderProfile.builder()
                .id("sp-managed")
                .profileType(ProfileType.MANAGED)
                .isGlobal(true)
                .createdBy("admin-1")
                .createdByRole(UserType.ASPIRE_ADMIN.getValue())
                .build();

        when(userCurrentContextService.getCurrentUserContext()).thenReturn(context);
        when(senderProfileRepository.findWithFilters(
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(List.of(own, other, managed));
        when(senderProfileMapper.toDto(any(SenderProfile.class), anyBoolean(), anyBoolean()))
                .thenReturn(SenderProfileDto.builder().build());

        senderProfileService.getSenderProfiles(
                0, 10, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                "createdAt", "desc");

        verify(senderProfileMapper).toDto(own, true, true);
        verify(senderProfileMapper).toDto(other, false, false);
        verify(senderProfileMapper).toDto(managed, false, false);
    }

    @Test
    void deleteSenderProfile_allowsAspireAdminForAnyProfile() {
        CurrentUserContext context = CurrentUserContext.builder()
                .userId("admin-1")
                .userType(UserType.ASPIRE_ADMIN.getValue())
                .build();
        SenderProfile managed = SenderProfile.builder()
                .id("6a60dc06e5ad47567aa13c24")
                .profileType(ProfileType.MANAGED)
                .isGlobal(true)
                .createdBy("other-admin")
                .build();

        when(userCurrentContextService.getCurrentUserContext()).thenReturn(context);
        when(senderProfileRepository.findByIdAndClientIdOrGlobal(eq("6a60dc06e5ad47567aa13c24"), isNull()))
                .thenReturn(Optional.of(managed));

        senderProfileService.deleteSenderProfile("6a60dc06e5ad47567aa13c24");

        verify(senderProfileRepository).delete(managed);
    }

    @Test
    void deleteSenderProfile_allowsCreator() {
        CurrentUserContext context = CurrentUserContext.builder()
                .clientAdminId("client-1")
                .userId("user-1")
                .userType(UserType.CLIENT_ADMIN.getValue())
                .build();
        SenderProfile own = SenderProfile.builder()
                .id("sp-own")
                .clientId("client-1")
                .profileType(ProfileType.CUSTOM)
                .isGlobal(false)
                .createdBy("user-1")
                .build();

        when(userCurrentContextService.getCurrentUserContext()).thenReturn(context);
        when(senderProfileRepository.findByIdAndClientIdOrGlobal("sp-own", "client-1"))
                .thenReturn(Optional.of(own));
        when(campaignRepository.findByClientIdAndSenderProfileId("client-1", "sp-own"))
                .thenReturn(Collections.emptyList());

        senderProfileService.deleteSenderProfile("sp-own");

        verify(senderProfileRepository).delete(own);
    }

    @Test
    void deleteSenderProfile_rejectsNonCreatorNonAdmin() {
        CurrentUserContext context = CurrentUserContext.builder()
                .clientAdminId("client-1")
                .userId("user-1")
                .userType(UserType.CLIENT_ADMIN.getValue())
                .build();
        SenderProfile other = SenderProfile.builder()
                .id("sp-other")
                .clientId("client-1")
                .profileType(ProfileType.CUSTOM)
                .isGlobal(false)
                .createdBy("user-2")
                .build();

        when(userCurrentContextService.getCurrentUserContext()).thenReturn(context);
        when(senderProfileRepository.findByIdAndClientIdOrGlobal("sp-other", "client-1"))
                .thenReturn(Optional.of(other));

        ServiceException exception = assertThrows(
                ServiceException.class, () -> senderProfileService.deleteSenderProfile("sp-other"));

        assertEquals("You do not have permission to delete this sender profile", exception.getMessage());
    }
}
