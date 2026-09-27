package com.aspire.asat.auth.service;

import com.aspire.asat.auth.entity.AspireUser;
import com.aspire.asat.auth.entity.ClientAdmin;
import com.aspire.asat.auth.entity.Timezone;
import com.aspire.asat.auth.repository.ClientAdminRepository;
import com.aspire.asat.auth.repository.TimezoneRepository;
import com.aspire.asat.common.enums.UserType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrgTimezoneServiceTest {

    private static final String CLIENT_ADMIN_ID = "ca-1";
    private static final String TZ_DOC_ID = "tz-1";

    @Mock private ClientAdminRepository clientAdminRepository;
    @Mock private TimezoneRepository timezoneRepository;

    @InjectMocks
    private OrgTimezoneService orgTimezoneService;

    @Test
    void formatForEmailUsesOffsetFromTimezoneDocument() {
        UUID userId = UUID.randomUUID();
        AspireUser user = AspireUser.builder()
                .userId(userId)
                .userType(UserType.USER.name())
                .clientAdminId(CLIENT_ADMIN_ID)
                .build();
        when(clientAdminRepository.findById(CLIENT_ADMIN_ID)).thenReturn(Optional.of(
                ClientAdmin.builder().id(CLIENT_ADMIN_ID).timeZone(TZ_DOC_ID).build()));
        when(timezoneRepository.findById(TZ_DOC_ID)).thenReturn(Optional.of(
                Timezone.builder()
                        .id(TZ_DOC_ID)
                        .timezoneId("SGT (UTC+08:00)")
                        .displayName("Singapore Time")
                        .build()));

        Instant instant = Instant.parse("2026-08-23T14:26:09Z");
        assertEquals("23 August 2026 at 10:26 PM", orgTimezoneService.formatForEmail(instant, user));
    }

    @Test
    void resolveFallsBackToUtcWhenTimezoneMissing() {
        AspireUser user = AspireUser.builder()
                .userId(UUID.randomUUID())
                .userType(UserType.USER.name())
                .clientAdminId(CLIENT_ADMIN_ID)
                .build();
        when(clientAdminRepository.findById(CLIENT_ADMIN_ID)).thenReturn(Optional.of(
                ClientAdmin.builder().id(CLIENT_ADMIN_ID).timeZone(null).build()));

        assertEquals(ZoneOffset.UTC, orgTimezoneService.resolveZoneForUser(user));
    }

    @Test
    void resolveAcceptsLegacyDirectOffsetOnClientAdmin() {
        when(clientAdminRepository.findById(CLIENT_ADMIN_ID)).thenReturn(Optional.of(
                ClientAdmin.builder().id(CLIENT_ADMIN_ID).timeZone("WAT (UTC+01:00)").build()));
        when(timezoneRepository.findById("WAT (UTC+01:00)")).thenReturn(Optional.empty());

        AspireUser user = AspireUser.builder()
                .userId(UUID.randomUUID())
                .userType(UserType.USER.name())
                .clientAdminId(CLIENT_ADMIN_ID)
                .build();

        assertEquals(ZoneOffset.of("+01:00"), orgTimezoneService.resolveZoneForUser(user));
    }
}
