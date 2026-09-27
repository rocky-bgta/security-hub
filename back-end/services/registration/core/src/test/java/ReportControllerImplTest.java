import com.aspire.asat.registration.controller.impl.ReportControllerImpl;
import com.aspire.asat.registration.data.apiResponses.AllResponseDto;
import com.aspire.asat.registration.data.apiResponses.ApiResponseDto;
import com.aspire.asat.registration.data.reports.UserSummaryReportDTO;
import com.aspire.asat.registration.data.reports.UserSummaryTotalsDTO;
import com.aspire.asat.registration.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReportControllerImplTest {

    @Mock
    private ReportService reportService;

    @InjectMocks
    private ReportControllerImpl reportController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getUserSummaryReport_shouldReturn200WithPayload() {
        UserSummaryReportDTO report = UserSummaryReportDTO.builder()
                .totals(UserSummaryTotalsDTO.builder()
                        .totalUsers(10)
                        .activeUsers(8)
                        .suspendedUsers(1)
                        .newSignupsLast30Days(2)
                        .build())
                .growthTrend(List.of())
                .details(new AllResponseDto<>(0, 20, 10L, List.of()))
                .build();

        when(reportService.getUserSummaryReport(
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), eq(0), eq(20), eq(6)))
                .thenReturn(report);

        ResponseEntity<ApiResponseDto<UserSummaryReportDTO>> response =
                reportController.getUserSummaryReport(
                        null, null, null, null, null, null,
                        null, null, 0, 20, 6);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(200, response.getBody().getStatusCode());
        assertEquals(10, response.getBody().getData().getTotals().getTotalUsers());
    }

    @Test
    void getUserSummaryReport_shouldConvertDateRangeToUtcInstants() {
        UserSummaryReportDTO report = UserSummaryReportDTO.builder()
                .totals(UserSummaryTotalsDTO.builder().build())
                .growthTrend(List.of())
                .details(new AllResponseDto<>(0, 20, 0L, List.of()))
                .build();

        when(reportService.getUserSummaryReport(
                any(), any(), any(), any(), any(), any(),
                any(), any(), anyInt(), anyInt(), anyInt()))
                .thenReturn(report);

        LocalDate fromDate = LocalDate.of(2026, 1, 1);
        LocalDate toDate = LocalDate.of(2026, 1, 31);

        reportController.getUserSummaryReport(
                null, null, null, null, null, null,
                fromDate, toDate, 0, 20, 6);

        ArgumentCaptor<Instant> fromCaptor = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<Instant> toCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(reportService, times(1)).getUserSummaryReport(
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                fromCaptor.capture(), toCaptor.capture(), eq(0), eq(20), eq(6));

        assertEquals(Instant.parse("2026-01-01T00:00:00Z"), fromCaptor.getValue());
        assertEquals(Instant.parse("2026-02-01T00:00:00Z"), toCaptor.getValue());
    }

    @Test
    void getUserSummaryReport_shouldReturn400WhenToDateBeforeFromDate() {
        ResponseEntity<ApiResponseDto<UserSummaryReportDTO>> response =
                reportController.getUserSummaryReport(
                        null, null, null, null, null, null,
                        LocalDate.of(2026, 3, 1), LocalDate.of(2026, 2, 1),
                        0, 20, 6);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().getStatusCode());
        assertNull(response.getBody().getData());
    }

    @Test
    void exportUserSummaryReport_shouldReturnCsvAttachment() {
        when(reportService.exportUserDetailsCsv(
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull()))
                .thenReturn("Name,Email\n".getBytes());

        ResponseEntity<Resource> response = reportController.exportUserSummaryReport(
                null, null, null, null, null, null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getHeaders().getFirst("Content-Disposition"));
        assertEquals("text/csv; charset=UTF-8", response.getHeaders().getFirst("Content-Type"));
        assertNotNull(response.getBody());
    }

    @Test
    void exportUserSummaryReport_shouldReturn400WhenDateRangeInvalid() {
        ResponseEntity<Resource> response = reportController.exportUserSummaryReport(
                null, null, null, null, null, null,
                LocalDate.of(2026, 5, 10), LocalDate.of(2026, 5, 1));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
