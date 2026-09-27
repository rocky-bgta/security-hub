package com.aspire.asat.registration.service.impl;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.registration.client.RegistrationNotificationClient;
import com.aspire.asat.registration.client.service.PhishingServiceClient;
import com.aspire.asat.registration.data.dto.AspireUserDto;
import com.aspire.asat.registration.data.dto.AspireUserUpdateRequestDto;
import com.aspire.asat.registration.data.phishing.request.UserLicenceSnapshotRequestDto;
import com.aspire.asat.registration.data.request.EndUserUpdateRequestDTO;
import com.aspire.asat.registration.mapper.AspireUserMapper;
import com.aspire.asat.registration.repository.AspireUserRepository;
import com.aspire.asat.registration.repository.ClientAdminRepository;
import com.aspire.asat.registration.repository.ClientProductRepository;
import com.aspire.asat.registration.repository.EndUserPackageRepository;
import com.aspire.asat.registration.repository.RoleRepository;
import com.aspire.asat.registration.repository.UserLicenceRepository;
import com.aspire.asat.registration.repository.UserSuspendReasonRepository;
import com.aspire.asat.registration.repository.custom.EndUserRepositoryCustom;
import com.aspire.asat.registration.repository.dropdown.CountryRepository;
import com.aspire.asat.registration.repository.msp.MspUsersRepository;
import com.aspire.asat.registration.service.AspireUserService;
import com.aspire.asat.registration.service.DepartmentService;
import com.aspire.asat.registration.service.support.SimulationProductResolver;
import com.aspire.asat.registration.service.support.UserSessionInvalidationHelper;
import com.aspire.asat.registration.utils.UserCurrentContextService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EndUserServiceImplUpdateEndUserTest {

    private static final UUID USER_ID = UUID.fromString("a1e35989-5081-447e-ada1-25b978ea316e");
    private static final String CLIENT_ADMIN_ID = "client-admin-1";

    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EndUserPackageRepository endUserPackageRepository;
    @Mock private AspireUserService aspireUserService;
    @Mock private DepartmentService departmentService;
    @Mock private RoleRepository roleRepository;
    @Mock private ClientProductRepository clientProductRepository;
    @Mock private CountryRepository countryRepository;
    @Mock private WebClient webClient;
    @Mock private UserLicenceRepository userLicenceRepository;
    @Mock private UserCurrentContextService currentContextService;
    @Mock private RegistrationNotificationClient notificationClient;
    @Mock private AspireUserRepository aspireUserRepository;
    @Mock private ClientAdminRepository clientAdminRepository;
    @Mock private MspUsersRepository mspUsersRepository;
    @Mock private UserSuspendReasonRepository userSuspendReasonRepository;
    @Mock private MongoTemplate mongoTemplate;
    @Mock private EndUserRepositoryCustom endUserRepositoryCustom;
    @Mock private UserSessionInvalidationHelper userSessionInvalidationHelper;
    @Mock private PhishingServiceClient phishingServiceClient;
    @Mock private AspireUserMapper aspireUserMapper;
    @Mock private SimulationProductResolver simulationProductResolver;

    @InjectMocks
    private EndUserServiceImpl endUserService;

    @BeforeEach
    void setUp() {
        when(currentContextService.getCurrentUserContext()).thenReturn(
                CurrentUserContext.builder().userId("admin-1").build());
    }

    @Test
    void updateEndUser_relevantFields_syncsLicenceSnapshot() {
        AspireUserDto existing = AspireUserDto.builder()
                .baseUserId(USER_ID)
                .clientAdminId(CLIENT_ADMIN_ID)
                .firstName("Old")
                .lastName("Name")
                .email("u@example.com")
                .status("ACTIVE")
                .build();
        AspireUserDto updated = AspireUserDto.builder()
                .baseUserId(USER_ID)
                .clientAdminId(CLIENT_ADMIN_ID)
                .firstName("Ada")
                .lastName("Lovelace")
                .phoneNumber("123")
                .department("HR")
                .country("Bangladesh")
                .email("u@example.com")
                .status("ACTIVE")
                .build();
        when(aspireUserService.getUserById(USER_ID)).thenReturn(Optional.of(existing));
        when(aspireUserService.updateUser(eq(USER_ID), any(AspireUserUpdateRequestDto.class)))
                .thenReturn(updated);

        EndUserUpdateRequestDTO request = new EndUserUpdateRequestDTO();
        request.setId(USER_ID.toString());
        request.setFirstName("Ada");
        request.setLastName("Lovelace");

        endUserService.updateEndUser(request);

        ArgumentCaptor<UserLicenceSnapshotRequestDto> captor =
                ArgumentCaptor.forClass(UserLicenceSnapshotRequestDto.class);
        verify(phishingServiceClient).syncLicensedUserSnapshot(captor.capture());
        UserLicenceSnapshotRequestDto sync = captor.getValue();
        assertEquals(USER_ID.toString(), sync.getUserId());
        assertEquals(CLIENT_ADMIN_ID, sync.getClientAdminId());
        assertEquals("Ada", sync.getFirstName());
        assertEquals("Lovelace", sync.getLastName());
        assertEquals("123", sync.getPhoneNumber());
        assertEquals("HR", sync.getDepartmentName());
        assertEquals("Bangladesh", sync.getCountryName());
        assertTrue(sync.getActive());
    }

    @Test
    void updateEndUser_profilePictureOnly_skipsSync() {
        AspireUserDto existing = AspireUserDto.builder()
                .baseUserId(USER_ID)
                .clientAdminId(CLIENT_ADMIN_ID)
                .firstName("Ada")
                .email("u@example.com")
                .status("ACTIVE")
                .build();
        when(aspireUserService.getUserById(USER_ID)).thenReturn(Optional.of(existing));
        when(aspireUserService.updateUser(eq(USER_ID), any(AspireUserUpdateRequestDto.class)))
                .thenReturn(existing);

        EndUserUpdateRequestDTO request = new EndUserUpdateRequestDTO();
        request.setId(USER_ID.toString());
        request.setProfilePicture("https://cdn.example.com/p.png");

        endUserService.updateEndUser(request);

        verify(phishingServiceClient, never()).syncLicensedUserSnapshot(any());
    }

    @Test
    void updateEndUser_syncFailure_throwsIllegalStateException() {
        AspireUserDto existing = AspireUserDto.builder()
                .baseUserId(USER_ID)
                .clientAdminId(CLIENT_ADMIN_ID)
                .firstName("Old")
                .email("u@example.com")
                .status("ACTIVE")
                .build();
        AspireUserDto updated = AspireUserDto.builder()
                .baseUserId(USER_ID)
                .clientAdminId(CLIENT_ADMIN_ID)
                .firstName("Ada")
                .email("u@example.com")
                .status("ACTIVE")
                .build();
        when(aspireUserService.getUserById(USER_ID)).thenReturn(Optional.of(existing));
        when(aspireUserService.updateUser(eq(USER_ID), any(AspireUserUpdateRequestDto.class)))
                .thenReturn(updated);
        doThrow(new IllegalStateException("Failed to sync licensed user snapshot to phishing service"))
                .when(phishingServiceClient).syncLicensedUserSnapshot(any());

        EndUserUpdateRequestDTO request = new EndUserUpdateRequestDTO();
        request.setId(USER_ID.toString());
        request.setFirstName("Ada");

        assertThrows(IllegalStateException.class, () -> endUserService.updateEndUser(request));
    }
}
