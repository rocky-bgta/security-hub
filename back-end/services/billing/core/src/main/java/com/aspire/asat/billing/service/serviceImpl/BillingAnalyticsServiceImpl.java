package com.aspire.asat.billing.service.serviceImpl;

import com.aspire.asat.billing.dto.analytics.*;
import com.aspire.asat.billing.repo.customRepo.BillingAnalyticsRepositoryCustom;
import com.aspire.asat.billing.service.BillingAnalyticsService;
import com.aspire.asat.billing.utils.UserCurrentContextService;
import com.aspire.asat.billing.utils.file.CsvGeneratorUtil;
import com.aspire.asat.billing.utils.file.PdfGeneratorService;
import com.aspire.asat.common.dto.files.CurrentUserContext;
import com.aspire.asat.common.enums.UserType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillingAnalyticsServiceImpl implements BillingAnalyticsService {

    private final BillingAnalyticsRepositoryCustom analyticsRepository;
    private final PdfGeneratorService pdfGeneratorService;
    private final UserCurrentContextService userCurrentContextService;

    @Override
    public BillingAnalyticsResponseDTO getAnalytics(AnalyticsPeriod period, String startDate, String endDate,
                                                     String mspId, String countryId) {
        Instant start = parseDate(startDate);
        Instant end = parseDate(endDate);

        // Set default date range if not provided
        if (start == null || end == null) {
            LocalDate now = LocalDate.now();
            switch (period) {
                case MONTHLY:
                    start = now.minusMonths(12).atStartOfDay(ZoneId.systemDefault()).toInstant();
                    break;
                case QUARTERLY:
                    start = now.minusMonths(12).atStartOfDay(ZoneId.systemDefault()).toInstant();
                    break;
                case YEARLY:
                    start = now.minusYears(5).atStartOfDay(ZoneId.systemDefault()).toInstant();
                    break;
            }
            end = Instant.now();
        }

        CurrentUserContext userContext = userCurrentContextService.getCurrentUserContext();
        String userType = userContext.getUserType();

        // Set mspId from Current Context whether UserType
        if (UserType.MSP.name().equals(userType)) {
            mspId = userContext.getUserId();
        }

        BillingAnalyticsSummaryDTO summary = buildSummary(period, start, end, mspId, countryId);
        List<RevenueByPeriodDTO> revenueTrend = analyticsRepository.getRevenueByPeriod(period, start, end, mspId, countryId);
        List<TopPackageDTO> topPackages = analyticsRepository.getTopPackages(start, end, mspId, countryId, 5);
        List<FailedPaymentAnalysisDTO> failedPayments = analyticsRepository.getFailedPaymentsByReason(start, end, mspId, countryId);
        PaymentSuccessRateDTO successRate = analyticsRepository.getPaymentSuccessRate(start, end, mspId, countryId);

        return BillingAnalyticsResponseDTO.builder()
                .summary(summary)
                .revenueTrend(revenueTrend)
                .topPackages(topPackages)
                .failedPaymentAnalysis(failedPayments)
                .paymentSuccessRate(successRate)
                .period(period)
                .startDate(formatDate(start))
                .endDate(formatDate(end))
                .build();
    }

    @Override
    public BillingAnalyticsSummaryDTO getSummary(AnalyticsPeriod period, String startDate, String endDate,
                                                  String mspId, String countryId) {
        Instant start = parseDate(startDate);
        Instant end = parseDate(endDate);

        // Set default date range if not provided
        if (start == null || end == null) {
            LocalDate now = LocalDate.now();
            start = now.withDayOfMonth(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
            end = Instant.now();
        }

        return buildSummary(period, start, end, mspId, countryId);
    }

    private BillingAnalyticsSummaryDTO buildSummary(AnalyticsPeriod period, Instant start, Instant end,
                                                     String mspId, String countryId) {
        // Current period metrics
        Double totalRevenue = analyticsRepository.getTotalRevenue(start, end, mspId, countryId);
        Double totalRefunds = analyticsRepository.getTotalRefunds(start, end, mspId, countryId);
        Long failedPaymentsCount = analyticsRepository.getFailedPaymentsCount(start, end, mspId, countryId);
        Long newSubscriptions = analyticsRepository.getNewSubscriptionsCount(start, end, mspId, countryId);
        Long activeLicenses = analyticsRepository.getActiveLicensesCount(mspId, countryId);

        // Calculate previous period for comparison
        long daysBetween = ChronoUnit.DAYS.between(start, end);
        Instant prevEnd = start;
        Instant prevStart = start.minus(daysBetween, ChronoUnit.DAYS);

        Double prevRevenue = analyticsRepository.getTotalRevenue(prevStart, prevEnd, mspId, countryId);
        Long prevSubscriptions = analyticsRepository.getNewSubscriptionsCount(prevStart, prevEnd, mspId, countryId);

        // Calculate percentage changes
        double revenueChange = calculatePercentageChange(prevRevenue, totalRevenue);
        double subscriptionsChange = calculatePercentageChange(
                prevSubscriptions != null ? prevSubscriptions.doubleValue() : 0.0,
                newSubscriptions != null ? newSubscriptions.doubleValue() : 0.0
        );

        // Calculate refund rate
        double refundRate = totalRevenue != null && totalRevenue > 0 && totalRefunds != null
                ? (totalRefunds / totalRevenue) * 100
                : 0.0;

        return BillingAnalyticsSummaryDTO.builder()
                .totalRevenue(totalRevenue != null ? totalRevenue : 0.0)
                .totalRevenueChangePercent(Math.round(revenueChange * 10.0) / 10.0)
                .totalRefunds(totalRefunds != null ? totalRefunds : 0.0)
                .refundRate(Math.round(refundRate * 10.0) / 10.0)
                .failedPaymentsCount(failedPaymentsCount != null ? failedPaymentsCount : 0L)
                .failedPaymentsLabel(getPeriodLabel(period))
                .newSubscriptions(newSubscriptions != null ? newSubscriptions : 0L)
                .newSubscriptionsChangePercent(Math.round(subscriptionsChange * 10.0) / 10.0)
                .activeLicenses(activeLicenses != null ? activeLicenses : 0L)
                .activeLicensesLabel("Total active subscriptions")
                .build();
    }

    private String getPeriodLabel(AnalyticsPeriod period) {
        switch (period) {
            case MONTHLY:
                return "This month";
            case QUARTERLY:
                return "This quarter";
            case YEARLY:
                return "This year";
            default:
                return "This period";
        }
    }

    private double calculatePercentageChange(Double previous, Double current) {
        if (previous == null || previous == 0.0) {
            return current != null && current > 0 ? 100.0 : 0.0;
        }
        if (current == null) {
            return -100.0;
        }
        return ((current - previous) / previous) * 100.0;
    }

    @Override
    public List<RevenueByPeriodDTO> getRevenueTrend(AnalyticsPeriod period, String startDate, String endDate,
                                                     String mspId, String countryId) {
        Instant start = parseDate(startDate);
        Instant end = parseDate(endDate);

        if (start == null || end == null) {
            LocalDate now = LocalDate.now();
            start = now.minusMonths(12).atStartOfDay(ZoneId.systemDefault()).toInstant();
            end = Instant.now();
        }

        return analyticsRepository.getRevenueByPeriod(period, start, end, mspId, countryId);
    }

    @Override
    public List<TopPackageDTO> getTopPackages(String startDate, String endDate, String mspId, String countryId, int limit) {
        Instant start = parseDate(startDate);
        Instant end = parseDate(endDate);

        if (start == null || end == null) {
            LocalDate now = LocalDate.now();
            start = now.minusMonths(12).atStartOfDay(ZoneId.systemDefault()).toInstant();
            end = Instant.now();
        }

        return analyticsRepository.getTopPackages(start, end, mspId, countryId, limit);
    }

    @Override
    public List<FailedPaymentAnalysisDTO> getFailedPaymentsAnalysis(String startDate, String endDate,
                                                                     String mspId, String countryId) {
        Instant start = parseDate(startDate);
        Instant end = parseDate(endDate);

        if (start == null || end == null) {
            LocalDate now = LocalDate.now();
            start = now.minusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
            end = Instant.now();
        }

        return analyticsRepository.getFailedPaymentsByReason(start, end, mspId, countryId);
    }

    @Override
    public PaymentSuccessRateDTO getPaymentSuccessRate(String startDate, String endDate, String mspId, String countryId) {
        Instant start = parseDate(startDate);
        Instant end = parseDate(endDate);

        if (start == null || end == null) {
            LocalDate now = LocalDate.now();
            start = now.minusMonths(12).atStartOfDay(ZoneId.systemDefault()).toInstant();
            end = Instant.now();
        }

        CurrentUserContext userContext = userCurrentContextService.getCurrentUserContext();
        String userType = userContext.getUserType();

        // Set mspId from Current Context whether UserType
        if (UserType.MSP.name().equals(userType)) {
            mspId = userContext.getUserId();
        }

        return analyticsRepository.getPaymentSuccessRate(start, end, mspId, countryId);
    }

    @Override
    public byte[] exportAnalyticsCsv(AnalyticsPeriod period, String startDate, String endDate,
                                      String mspId, String countryId) {
        BillingAnalyticsResponseDTO analytics = getAnalytics(period, startDate, endDate, mspId, countryId);
        ByteArrayOutputStream output = CsvGeneratorUtil.generateAnalyticsCsv(analytics);
        return output.toByteArray();
    }

    @Override
    public byte[] exportAnalyticsPdf(AnalyticsPeriod period, String startDate, String endDate,
                                      String mspId, String countryId) {
        BillingAnalyticsResponseDTO analytics = getAnalytics(period, startDate, endDate, mspId, countryId);
        ByteArrayOutputStream output = pdfGeneratorService.generateAnalyticsPdf(analytics);
        return output.toByteArray();
    }

    private Instant parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr).atStartOfDay(ZoneId.systemDefault()).toInstant();
        } catch (Exception e) {
            log.warn("Failed to parse date: {}", dateStr, e);
            return null;
        }
    }

    private String formatDate(Instant instant) {
        if (instant == null) {
            return null;
        }
        return instant.atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}

