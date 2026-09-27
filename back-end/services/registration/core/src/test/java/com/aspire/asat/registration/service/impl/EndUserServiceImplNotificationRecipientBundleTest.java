package com.aspire.asat.registration.service.impl;

import com.aspire.asat.common.dto.notification.NotificationRecipientBundleDto;
import com.aspire.asat.registration.client.RegistrationNotificationClient;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.registration.data.clientAdmin.request.AdminStatus;
import com.aspire.asat.registration.model.AspireUser;
import com.aspire.asat.registration.model.ClientAdmin;
import com.aspire.asat.registration.model.msp.MspUser;
import com.aspire.asat.registration.model.dropdown.Timezone;
import com.aspire.asat.registration.repository.AspireUserRepository;
import com.aspire.asat.registration.repository.ClientAdminRepository;
import com.aspire.asat.registration.repository.ClientProductRepository;
import com.aspire.asat.registration.repository.EndUserPackageRepository;
import com.aspire.asat.registration.repository.RoleRepository;
import com.aspire.asat.registration.repository.UserLicenceRepository;
import com.aspire.asat.registration.repository.UserSuspendReasonRepository;
import com.aspire.asat.registration.repository.custom.EndUserRepositoryCustom;
import com.aspire.asat.registration.repository.dropdown.CountryRepository;
import com.aspire.asat.registration.repository.dropdown.TimezoneRepository;
import com.aspire.asat.registration.repository.msp.MspUsersRepository;
import com.aspire.asat.registration.service.AspireUserService;
import com.aspire.asat.registration.service.DepartmentService;
import com.aspire.asat.registration.service.support.SimulationProductResolver;
import com.aspire.asat.registration.service.support.UserSessionInvalidationHelper;
import com.aspire.asat.registration.utils.UserCurrentContextService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EndUserServiceImplNotificationRecipientBundleTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID CLIENT_ADMIN_USER_ID = UUID.randomUUID();
    private static final UUID ASPIRE_ADMIN_USER_ID = UUID.randomUUID();
    private static final String CLIENT_ADMIN_ID = "client-admin-1";
    private static final String MSP_ID = "msp-1";
    private static final String TIMEZONE_DOC_ID = "tz-doc-1";

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
    @Mock private SimulationProductResolver simulationProductResolver;
    @Mock private TimezoneRepository timezoneRepository;

    @InjectMocks
    private EndUserServiceImpl endUserService;

    @Test
    void getNotificationRecipientBundle_userNotFound_returnsEmpty() {
        when(aspireUserRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());

        Optional<NotificationRecipientBundleDto> result =
                endUserService.getNotificationRecipientBundle(USER_ID.toString());

        assertFalse(result.isPresent());
    }

    @Test
    void getNotificationRecipientBundle_invalidUserId_returnsEmpty() {
        Optional<NotificationRecipientBundleDto> result =
                endUserService.getNotificationRecipientBundle("not-a-uuid");

        assertFalse(result.isPresent());
    }

    @Test
    void getNotificationRecipientBundle_userWithoutClientAdmin_returnsUserAndAspireAdminsOnly() {
        AspireUser endUser = AspireUser.builder()
                .userId(USER_ID)
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@example.com")
                .phoneNumber("5551234")
                .phoneCode("+1")
                .clientAdminId(null)
                .build();
        when(aspireUserRepository.findByUserId(USER_ID)).thenReturn(Optional.of(endUser));
        when(aspireUserRepository.findByUserType(UserType.ASPIRE_ADMIN.name()))
                .thenReturn(List.of(activeAspireAdmin()));

        Optional<NotificationRecipientBundleDto> result =
                endUserService.getNotificationRecipientBundle(USER_ID.toString());

        assertTrue(result.isPresent());
        NotificationRecipientBundleDto bundle = result.get();
        assertEquals(USER_ID.toString(), bundle.getUserId());
        assertEquals("Jane Doe", bundle.getUserFullName());
        assertEquals("jane.doe@example.com", bundle.getEmail());
        assertEquals(null, bundle.getClientAdminId());
        assertEquals(null, bundle.getMspId());
        assertEquals(1, bundle.getAspireAdmins().size());
        assertEquals(ASPIRE_ADMIN_USER_ID.toString(), bundle.getAspireAdmins().get(0).getUserId());
    }

    @Test
    void getNotificationRecipientBundle_userWithClientAdminAndMsp_populatesFullHierarchy() {
        AspireUser endUser = AspireUser.builder()
                .userId(USER_ID)
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@example.com")
                .clientAdminId(CLIENT_ADMIN_ID)
                .build();
        ClientAdmin clientAdmin = ClientAdmin.builder()
                .id(CLIENT_ADMIN_ID)
                .organizationName("Acme Corp")
                .email("admin@acme.com")
                .mspId(MSP_ID)
                .timeZone(TIMEZONE_DOC_ID)
                .status(AdminStatus.ACTIVE)
                .build();
        Timezone timezone = Timezone.builder()
                .id(TIMEZONE_DOC_ID)
                .timezoneId("WAT (UTC+01:00)")
                .displayName("West Africa Time")
                .build();
        AspireUser clientAdminUser = AspireUser.builder()
                .userId(CLIENT_ADMIN_USER_ID)
                .userType(UserType.CLIENT_ADMIN.name())
                .clientAdminId(CLIENT_ADMIN_ID)
                .build();
        MspUser mspUser = MspUser.builder()
                .id(MSP_ID)
                .organizationName("Reseller Inc")
                .mspAdminEmail("msp@reseller.com")
                .build();

        when(aspireUserRepository.findByUserId(USER_ID)).thenReturn(Optional.of(endUser));
        when(clientAdminRepository.findById(CLIENT_ADMIN_ID)).thenReturn(Optional.of(clientAdmin));
        when(timezoneRepository.findById(TIMEZONE_DOC_ID)).thenReturn(Optional.of(timezone));
        when(aspireUserRepository.findByUserTypeAndClientAdminId(UserType.CLIENT_ADMIN.name(), CLIENT_ADMIN_ID))
                .thenReturn(List.of(clientAdminUser));
        when(mspUsersRepository.findById(MSP_ID)).thenReturn(Optional.of(mspUser));
        when(aspireUserRepository.findByUserType(UserType.ASPIRE_ADMIN.name())).thenReturn(List.of());

        Optional<NotificationRecipientBundleDto> result =
                endUserService.getNotificationRecipientBundle(USER_ID.toString());

        assertTrue(result.isPresent());
        NotificationRecipientBundleDto bundle = result.get();
        assertEquals(CLIENT_ADMIN_ID, bundle.getClientAdminId());
        assertEquals("Acme Corp", bundle.getClientAdminName());
        assertEquals("admin@acme.com", bundle.getClientAdminEmail());
        assertEquals(CLIENT_ADMIN_USER_ID.toString(), bundle.getClientAdminUserId());
        assertEquals(MSP_ID, bundle.getMspId());
        assertEquals("Reseller Inc", bundle.getMspName());
        assertEquals("msp@reseller.com", bundle.getMspEmail());
        assertEquals(TIMEZONE_DOC_ID, bundle.getOrganizationTimezoneRef());
        assertEquals("WAT (UTC+01:00)", bundle.getOrganizationTimezoneLabel());
        assertEquals("West Africa Time", bundle.getOrganizationTimezoneDisplayName());
        assertTrue(bundle.getAspireAdmins().isEmpty());
    }

    @Test
    void getNotificationRecipientBundle_clientAdminNotFound_stillReturnsUserAndAspireAdmins() {
        AspireUser endUser = AspireUser.builder()
                .userId(USER_ID)
                .firstName("Jane")
                .lastName("Doe")
                .clientAdminId(CLIENT_ADMIN_ID)
                .build();
        when(aspireUserRepository.findByUserId(USER_ID)).thenReturn(Optional.of(endUser));
        when(clientAdminRepository.findById(CLIENT_ADMIN_ID)).thenReturn(Optional.empty());
        when(aspireUserRepository.findByUserType(UserType.ASPIRE_ADMIN.name())).thenReturn(List.of());

        Optional<NotificationRecipientBundleDto> result =
                endUserService.getNotificationRecipientBundle(USER_ID.toString());

        assertTrue(result.isPresent());
        NotificationRecipientBundleDto bundle = result.get();
        assertEquals(CLIENT_ADMIN_ID, bundle.getClientAdminId());
        assertEquals(null, bundle.getClientAdminName());
        assertEquals(null, bundle.getMspId());
    }

    private static AspireUser activeAspireAdmin() {
        return AspireUser.builder()
                .userId(ASPIRE_ADMIN_USER_ID)
                .firstName("Alice")
                .lastName("Admin")
                .email("alice.admin@aspire.com")
                .status("ACTIVE")
                .userType(UserType.ASPIRE_ADMIN.name())
                .build();
    }
}
