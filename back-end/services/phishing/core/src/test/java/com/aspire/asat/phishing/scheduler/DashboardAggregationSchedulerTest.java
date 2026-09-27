package com.aspire.asat.phishing.scheduler;

import com.aspire.asat.phishing.service.DashboardAggregationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DashboardAggregationSchedulerTest {

    @Mock
    private DashboardAggregationService dashboardAggregationService;

    @InjectMocks
    private DashboardAggregationScheduler scheduler;

    @Test
    void aggregateDashboardStatsShouldRunWhenEnabled() {
        setField(scheduler, "dashboardAggregationEnabled", true);

        scheduler.aggregateDashboardStats();

        verify(dashboardAggregationService).aggregateForAllClients();
    }

    @Test
    void aggregateDashboardStatsShouldSkipWhenDisabled() {
        setField(scheduler, "dashboardAggregationEnabled", false);

        scheduler.aggregateDashboardStats();

        verify(dashboardAggregationService, never()).aggregateForAllClients();
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
