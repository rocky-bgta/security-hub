package com.aspire.asat.auth.service;

import com.aspire.asat.auth.entity.AspireUser;
import com.aspire.asat.auth.exception.UnauthorizedResourceException;
import com.aspire.asat.auth.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MfaIdentityServiceTest {

    @Mock
    private TemporaryTokenService temporaryTokenService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private MfaIdentityService mfaIdentityService;

    private UUID userId;
    private AspireUser user;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = AspireUser.builder()
                .id(userId)
                .userId(userId)
                .username("fireimclient-admin")
                .build();
        mfaIdentityService.setHttpServletRequest(httpServletRequest);
        mfaIdentityService = spy(mfaIdentityService);
    }

    @Test
    void resolveUserId_TempTokenPresent_PrefersTempTokenOverBearer() {
        when(temporaryTokenService.validateAndExtractUserId("temp-token")).thenReturn(userId);

        UUID resolved = mfaIdentityService.resolveUserId("temp-token");

        assertEquals(userId, resolved);
        verify(temporaryTokenService).validateAndExtractUserId("temp-token");
        verify(httpServletRequest, never()).getHeader("Authorization");
        verify(userRepository, never()).findByUserId(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void resolveUserId_ValidAccessJwt_ReturnsDocumentId() {
        when(httpServletRequest.getHeader("CurrentContext")).thenReturn(null);
        when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer access.jwt");
        doReturn(userId).when(mfaIdentityService).parseAccessTokenUserId("access.jwt");
        when(userRepository.findByUserId(userId)).thenReturn(Optional.of(user));

        UUID resolved = mfaIdentityService.resolveUserId(null);

        assertEquals(userId, resolved);
    }

    @Test
    void resolveUserId_ValidAccessJwt_FallsBackToFindById() {
        when(httpServletRequest.getHeader("CurrentContext")).thenReturn(null);
        when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer access.jwt");
        doReturn(userId).when(mfaIdentityService).parseAccessTokenUserId("access.jwt");
        when(userRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        UUID resolved = mfaIdentityService.resolveUserId("  ");

        assertEquals(userId, resolved);
    }

    @Test
    void resolveUserId_MissingTempTokenAndBearer_ThrowsUnauthorized() {
        when(httpServletRequest.getHeader("CurrentContext")).thenReturn(null);
        when(httpServletRequest.getHeader("Authorization")).thenReturn(null);

        UnauthorizedResourceException ex = assertThrows(
                UnauthorizedResourceException.class, () -> mfaIdentityService.resolveUserId(null));
        assertEquals(MfaIdentityService.AUTH_REQUIRED_MESSAGE, ex.getMessage());
    }

    @Test
    void resolveUserId_InvalidTokenType_ThrowsUnauthorized() {
        when(httpServletRequest.getHeader("CurrentContext")).thenReturn(null);
        when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer refresh.jwt");
        doThrow(new UnauthorizedResourceException("Invalid token type"))
                .when(mfaIdentityService).parseAccessTokenUserId("refresh.jwt");

        UnauthorizedResourceException ex = assertThrows(
                UnauthorizedResourceException.class, () -> mfaIdentityService.resolveUserId(null));
        assertEquals("Invalid token type", ex.getMessage());
    }
}
