package com.aspire.asat.auth.service;

import com.aspire.asat.auth.entity.AspireUser;
import com.aspire.asat.auth.entity.ClientAdmin;
import com.aspire.asat.auth.entity.Timezone;
import com.aspire.asat.auth.repository.ClientAdminRepository;
import com.aspire.asat.auth.repository.TimezoneRepository;
import com.aspire.asat.common.enums.UserType;
import com.aspire.asat.common.util.DatetimeUtils;
import com.aspire.asat.common.util.TimezoneResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

/**
 * Resolves organization timezone for user-facing timestamps (emails, notifications).
 * Uses {@code client_admins.timeZone} → {@code timezones} document → {@link TimezoneResolver}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrgTimezoneService {

    private final ClientAdminRepository clientAdminRepository;
    private final TimezoneRepository timezoneRepository;

    public ZoneId resolveZoneForUser(AspireUser user) {
        String clientAdminId = resolveClientAdminId(user);
        if (clientAdminId == null) {
            return TimezoneResolver.resolve(null);
        }
        Optional<ClientAdmin> clientAdminOpt = clientAdminRepository.findById(clientAdminId);
        if (clientAdminOpt.isEmpty()) {
            return TimezoneResolver.resolve(null);
        }
        return resolveZoneFromTimezoneRef(clientAdminOpt.get().getTimeZone());
    }

    public String formatForEmail(Instant instant, AspireUser user) {
        return DatetimeUtils.formatForEmail(instant, resolveZoneForUser(user));
    }

    public ZoneId resolveZoneFromTimezoneRef(String timezoneRef) {
        if (timezoneRef == null || timezoneRef.isBlank()) {
            return TimezoneResolver.resolve(null);
        }
        String ref = timezoneRef.trim();
        Optional<Timezone> timezoneOpt = timezoneRepository.findById(ref);
        if (timezoneOpt.isPresent()) {
            Timezone timezone = timezoneOpt.get();
            return TimezoneResolver.resolve(
                    timezone.getTimezoneId(),
                    timezone.getDisplayName());
        }
        // Legacy: client_admins.timeZone may already hold an IANA id or offset label
        ZoneId direct = TimezoneResolver.tryResolve(ref);
        if (direct != null) {
            return direct;
        }
        log.warn("Unable to resolve timezone ref '{}'; falling back to UTC", ref);
        return TimezoneResolver.resolve(null);
    }

    private String resolveClientAdminId(AspireUser user) {
        if (user == null) {
            return null;
        }
        if (UserType.CLIENT_ADMIN.getValue().equalsIgnoreCase(user.getUserType())) {
            return user.getUserId() != null ? user.getUserId().toString() : null;
        }
        if (!ObjectUtils.isEmpty(user.getClientAdminId())) {
            return user.getClientAdminId();
        }
        return null;
    }
}
