package com.aspire.asat.auth.service;

import com.aspire.asat.auth.config.MfaConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TemporaryTokenServiceOtpTtlTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private MfaConfig mfaConfig;

    @InjectMocks
    private TemporaryTokenService temporaryTokenService;

    @BeforeEach
    void setUp() {
        when(mfaConfig.getTempTokenExpirationMinutes()).thenReturn(30);
    }

    @Test
    void tempTokenExpirationMinutes_UsesConfiguredThirtyMinutes() throws Exception {
        Method method = TemporaryTokenService.class.getDeclaredMethod("tempTokenExpirationMinutes");
        method.setAccessible(true);

        int minutes = (int) method.invoke(temporaryTokenService);

        assertEquals(30, minutes);
    }

    @Test
    void tempTokenExpirationMinutes_FallsBackWhenInvalid() throws Exception {
        when(mfaConfig.getTempTokenExpirationMinutes()).thenReturn(0);
        Method method = TemporaryTokenService.class.getDeclaredMethod("tempTokenExpirationMinutes");
        method.setAccessible(true);

        int minutes = (int) method.invoke(temporaryTokenService);

        assertEquals(30, minutes);
    }
}
