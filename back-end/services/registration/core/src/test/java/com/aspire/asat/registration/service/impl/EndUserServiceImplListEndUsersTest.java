package com.aspire.asat.registration.service.impl;

import com.aspire.asat.registration.client.RegistrationNotificationClient;
import com.aspire.asat.registration.client.service.PhishingServiceClient;
import com.aspire.asat.registration.data.apiResponses.AllResponseDto;
import com.aspire.asat.registration.data.dto.AspireUserDto;
import com.aspire.asat.registration.data.endUser.response.EndUserResponseDTO;
import com.aspire.asat.registration.mapper.AspireUserMapper;
import com.aspire.asat.registration.model.AspireUser;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EndUserServiceImplListEndUsersTest {

    private static final String CLIENT_ADMIN_ID = "client-admin-1";
    private static final String PRODUCT_PACKAGE_ID = "pp-1";
    private static final UUID USER_ID = UUID.fromString("a1e35989-5081-447e-ada1-25b978ea316e");
    private static final UUID LICENSED_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

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

    @Test
    void listAndCountEndUsers_withoutProductPackageId_doesNotCallPhishing() {
        AspireUser entity = sampleUser(USER_ID);
        AspireUserDto dto = sampleDto(USER_ID);
        when(aspireUserRepository.countEndUsersPaged(
                eq(CLIENT_ADMIN_ID), isNull(), isNull(), isNull(), isNull(), eq(List.of())))
                .thenReturn(1L);
        when(aspireUserRepository.findEndUsersPaged(
                eq(CLIENT_ADMIN_ID), isNull(), isNull(), isNull(), isNull(), eq(List.of()), eq(0), eq(10)))
                .thenReturn(List.of(entity));
        when(clientAdminRepository.findById(CLIENT_ADMIN_ID)).thenReturn(Optional.empty());
        when(aspireUserMapper.toDto(entity)).thenReturn(dto);

        AllResponseDto<List<EndUserResponseDTO>> result = endUserService.listAndCountEndUsers(
                CLIENT_ADMIN_ID, null, null, null, null, 0, 10, null);

        assertEquals(1L, result.getTotal());
        assertEquals(1, result.getItems().size());
        verify(phishingServiceClient, never()).getLicensedUserIds(anyString(), anyString());
    }

    @Test
    void listAndCountEndUsers_withProductPackageId_excludesLicensedUsers() {
        when(phishingServiceClient.getLicensedUserIds(CLIENT_ADMIN_ID, PRODUCT_PACKAGE_ID))
                .thenReturn(List.of(LICENSED_ID.toString()));

        AspireUser entity = sampleUser(USER_ID);
        AspireUserDto dto = sampleDto(USER_ID);
        when(aspireUserRepository.countEndUsersPaged(
                eq(CLIENT_ADMIN_ID), isNull(), isNull(), isNull(), isNull(), eq(List.of(LICENSED_ID))))
                .thenReturn(1L);
        when(aspireUserRepository.findEndUsersPaged(
                eq(CLIENT_ADMIN_ID), isNull(), isNull(), isNull(), isNull(),
                eq(List.of(LICENSED_ID)), eq(1), eq(5)))
                .thenReturn(List.of(entity));
        when(clientAdminRepository.findById(CLIENT_ADMIN_ID)).thenReturn(Optional.empty());
        when(aspireUserMapper.toDto(entity)).thenReturn(dto);

        AllResponseDto<List<EndUserResponseDTO>> result = endUserService.listAndCountEndUsers(
                CLIENT_ADMIN_ID, null, null, null, null, 1, 5, PRODUCT_PACKAGE_ID);

        assertEquals(1L, result.getTotal());
        assertTrue(result.getItems().stream().noneMatch(u -> LICENSED_ID.toString().equals(u.getId())));
        verify(phishingServiceClient).getLicensedUserIds(CLIENT_ADMIN_ID, PRODUCT_PACKAGE_ID);
    }

    private static AspireUser sampleUser(UUID userId) {
        return AspireUser.builder()
                .userId(userId)
                .firstName("Tamim")
                .lastName("User")
                .email("tamim@example.com")
                .clientAdminId(CLIENT_ADMIN_ID)
                .userType("USER")
                .status("ACTIVE")
                .createdAt(Instant.now())
                .build();
    }

    private static AspireUserDto sampleDto(UUID userId) {
        return AspireUserDto.builder()
                .baseUserId(userId)
                .firstName("Tamim")
                .lastName("User")
                .email("tamim@example.com")
                .clientAdminId(CLIENT_ADMIN_ID)
                .status("ACTIVE")
                .createdAt(Instant.now())
                .build();
    }
}
