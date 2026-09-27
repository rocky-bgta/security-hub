package com.aspire.asat.universal.service;

import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.universal.entity.Policy;
import com.aspire.asat.universal.policy.PolicyDto;
import com.aspire.asat.universal.repository.PolicyRepository;
import com.aspire.asat.universal.repository.PolicyTypeRepository;
import com.aspire.asat.universal.utils.UserCurrentContextService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PolicyServiceGetPoliciesForCurrentUserTest {

    private static final String CLIENT_ADMIN_ID = "client-admin-1";
    private static final String MSP_ID = "msp-1";

    @Mock
    private PolicyRepository policyRepository;
    @Mock
    private PolicyTypeRepository policyTypeRepository;
    @Mock
    private UserCurrentContextService userCurrentContextService;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private ExternalApiService externalApiService;

    @InjectMocks
    private PolicyService policyService;

    @BeforeEach
    void setUp() {
        lenient().when(modelMapper.map(any(Policy.class), eq(PolicyDto.class))).thenReturn(new PolicyDto());
        lenient().when(policyRepository.findByClientAdminIdWithFilters(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(emptyPage());
        lenient().when(policyRepository.findPoliciesForClientAdminWithFilters(any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(emptyPage());
        lenient().when(policyRepository.findByMspIdWithFilters(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(emptyPage());
        lenient().when(policyRepository.findPoliciesForMspAdminWithFilters(any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(emptyPage());
        lenient().when(policyRepository.findPoliciesForClientUserWithFilters(any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(emptyPage());
    }

    @Test
    void getPoliciesForCurrentUser_clientAdminOwnPolicy_usesClientAdminFilter() {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(clientAdminContext());

        policyService.getPoliciesForCurrentUser(9, 0, null, null, null, true);

        verify(policyRepository).findByClientAdminIdWithFilters(
                eq(CLIENT_ADMIN_ID), isNull(), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    void getPoliciesForCurrentUser_clientAdminAllPolicies_usesScopedFilter() {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(clientAdminContext());

        policyService.getPoliciesForCurrentUser(9, 0, null, null, null, false);

        verify(policyRepository).findPoliciesForClientAdminWithFilters(
                eq(CLIENT_ADMIN_ID), eq(MSP_ID), isNull(), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    void getPoliciesForCurrentUser_mspOwnPolicy_usesMspFilter() {
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(mspContext());

        policyService.getPoliciesForCurrentUser(9, 0, null, null, null, true);

        verify(policyRepository).findByMspIdWithFilters(
                eq(MSP_ID), isNull(), isNull(), isNull(), any(Pageable.class));
    }

    @Test
    void getPoliciesForCurrentUser_endUserOwnPolicy_usesClientAdminFilter() {
        CurrentUserContext context = CurrentUserContext.builder()
                .userType(UserType.USER.getValue())
                .clientAdminId(CLIENT_ADMIN_ID)
                .build();
        when(userCurrentContextService.getCurrentUserContext()).thenReturn(context);

        policyService.getPoliciesForCurrentUser(9, 0, null, null, null, true);

        verify(policyRepository).findByClientAdminIdWithFilters(
                eq(CLIENT_ADMIN_ID), isNull(), isNull(), isNull(), any(Pageable.class));
    }

    private static CurrentUserContext clientAdminContext() {
        return CurrentUserContext.builder()
                .userId(CLIENT_ADMIN_ID)
                .userType(UserType.CLIENT_ADMIN.getValue())
                .mspId(MSP_ID)
                .build();
    }

    private static CurrentUserContext mspContext() {
        return CurrentUserContext.builder()
                .userId(MSP_ID)
                .userType(UserType.MSP.getValue())
                .build();
    }

    private static Page<Policy> emptyPage() {
        return new PageImpl<>(List.of());
    }
}
