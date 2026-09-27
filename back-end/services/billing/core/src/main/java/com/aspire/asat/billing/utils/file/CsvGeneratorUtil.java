package com.aspire.asat.billing.utils.file;

import com.aspire.asat.billing.dto.CouponCreateResponseDTO;
import com.aspire.asat.billing.dto.InvoiceHistoryItemDTO;
import com.aspire.asat.billing.dto.PaymentHistoryItemDTO;
import com.aspire.asat.billing.dto.ProductRestrictionDTO;
import com.aspire.asat.billing.dto.analytics.*;
import com.aspire.asat.billing.dto.invoice.InvoiceResponseDTO;
import com.aspire.asat.billing.dto.invoice.InvoiceStatus;
import com.aspire.asat.billing.dto.invoice.PaymentMethodType;
import com.aspire.asat.billing.dto.invoice.ProductSelectionDto;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class CsvGeneratorUtil {

    public static ByteArrayOutputStream generatePaymentHistoryCsv(List<PaymentHistoryItemDTO> items) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             PrintWriter writer = new PrintWriter(out, true, StandardCharsets.UTF_8)) {

            // Write BOM for UTF-8 to ensure Excel recognizes UTF-8
            out.write(0xEF);
            out.write(0xBB);
            out.write(0xBF);

            // Header row - existing columns retained; coupon/discount and related payment details appended
            writer.println("SL No,Payment ID,Invoice ID,Invoice Number,Client ID,MSP ID,Invoice Date,Payment Date,Total Amount,Amount Paid,Outstanding,Due Amount,Status,Payment Method,Client Name,MSP Name,Country Name,Role Type,Currency,Subtotal,VAT Amount,Discount Type,Discount Amount,Discount Percentage,Coupon Code,Coupon Discount Amount,Actual Amount,Transaction ID,Payment Type");

            // Data rows
            for (int i = 0; i < items.size(); i++) {
                PaymentHistoryItemDTO item = items.get(i);
                String invoiceDate = item.getInvoiceDate() != null 
                    ? item.getInvoiceDate().toString() 
                    : "";
                String paymentDate = item.getDate() != null ? item.getDate() : "";
                String roleType = item.getRoleType() != null ? item.getRoleType().name() : "";
                
                writer.printf("%d,%s,%s,%s,%s,%s,%s,%s,%.2f,%.2f,%.2f,%.2f,%s,%s,%s,%s,%s,%s,%s,%.2f,%.2f,%s,%.2f,%.2f,%s,%.2f,%.2f,%s,%s%n",
                    i + 1,
                    escapeCsv(item.getId()), // Payment ID
                    escapeCsv(item.getInvoiceId()),
                    escapeCsv(item.getInvoiceNumber()),
                    escapeCsv(item.getClientId()),
                    escapeCsv(item.getMspId()),
                    escapeCsv(invoiceDate),
                    escapeCsv(paymentDate),
                    item.getTotalAmount() != null ? item.getTotalAmount() : 0.0,
                    item.getAmount() != null ? item.getAmount() : 0.0,
                    item.getOutstanding() != null ? item.getOutstanding() : 0.0,
                    item.getDueAmount() != null ? item.getDueAmount() : 0.0,
                    escapeCsv(item.getStatus()),
                    escapeCsv(item.getPaymentMethod()),
                    escapeCsv(item.getClientName()),
                    escapeCsv(item.getMspName()),
                    escapeCsv(item.getCountryName()),
                    escapeCsv(roleType),
                    escapeCsv(item.getCurrency()),
                    item.getSubtotal() != null ? item.getSubtotal() : 0.0,
                    item.getVatAmount() != null ? item.getVatAmount() : 0.0,
                    escapeCsv(item.getDiscountType()),
                    item.getDiscountAmount() != null ? item.getDiscountAmount() : 0.0,
                    item.getDiscountPercentage() != null ? item.getDiscountPercentage() : 0.0,
                    escapeCsv(item.getCouponCode()),
                    item.getCouponDiscountAmount() != null ? item.getCouponDiscountAmount() : 0.0,
                    item.getActualAmount() != null ? item.getActualAmount() : 0.0,
                    escapeCsv(item.getTransactionId()),
                    escapeCsv(item.getPaymentType())
                );
            }

            writer.flush();
            return out;

        } catch (Exception e) {
            log.error("Failed to generate CSV file", e);
            throw new RuntimeException("Failed to generate CSV", e);
        }
    }

    /**
     * Generate analytics data as CSV.
     */
    public static ByteArrayOutputStream generateAnalyticsCsv(BillingAnalyticsResponseDTO analytics) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             PrintWriter writer = new PrintWriter(out, true, StandardCharsets.UTF_8)) {

            // Write BOM for UTF-8 to ensure Excel recognizes UTF-8
            out.write(0xEF);
            out.write(0xBB);
            out.write(0xBF);

            // Report metadata
            writer.println("BILLING ANALYTICS REPORT");
            writer.printf("Period: %s%n", analytics.getPeriod());
            writer.printf("Date Range: %s to %s%n", analytics.getStartDate(), analytics.getEndDate());
            writer.println();

            // Summary Section
            writer.println("=== SUMMARY ===");
            BillingAnalyticsSummaryDTO summary = analytics.getSummary();
            if (summary != null) {
                writer.printf("Total Revenue,$%.2f%n", summary.getTotalRevenue() != null ? summary.getTotalRevenue() : 0.0);
                writer.printf("Revenue Change (%%),%.1f%%%n", summary.getTotalRevenueChangePercent() != null ? summary.getTotalRevenueChangePercent() : 0.0);
                writer.printf("Total Refunds,$%.2f%n", summary.getTotalRefunds() != null ? summary.getTotalRefunds() : 0.0);
                writer.printf("Refund Rate,%.1f%%%n", summary.getRefundRate() != null ? summary.getRefundRate() : 0.0);
                writer.printf("Failed Payments,%d%n", summary.getFailedPaymentsCount() != null ? summary.getFailedPaymentsCount() : 0);
                writer.printf("New Subscriptions,%d%n", summary.getNewSubscriptions() != null ? summary.getNewSubscriptions() : 0);
                writer.printf("Subscriptions Change (%%),%.1f%%%n", summary.getNewSubscriptionsChangePercent() != null ? summary.getNewSubscriptionsChangePercent() : 0.0);
                writer.printf("Active Licenses,%d%n", summary.getActiveLicenses() != null ? summary.getActiveLicenses() : 0);
            }
            writer.println();

            // Revenue Trend Section
            writer.println("=== REVENUE TREND ===");
            writer.println("Period,Revenue,New Subscriptions,Refunds,Net Revenue");
            List<RevenueByPeriodDTO> revenueTrend = analytics.getRevenueTrend();
            if (revenueTrend != null) {
                for (RevenueByPeriodDTO item : revenueTrend) {
                    writer.printf("%s,$%.2f,%d,$%.2f,$%.2f%n",
                            escapeCsv(item.getPeriod()),
                            item.getRevenue() != null ? item.getRevenue() : 0.0,
                            item.getNewSubscriptions() != null ? item.getNewSubscriptions() : 0,
                            item.getRefunds() != null ? item.getRefunds() : 0.0,
                            item.getNetRevenue() != null ? item.getNetRevenue() : 0.0
                    );
                }
            }
            writer.println();

            // Top Packages Section
            writer.println("=== TOP PACKAGES ===");
            writer.println("Package Name,Subscriptions,Revenue,Revenue %");
            List<TopPackageDTO> topPackages = analytics.getTopPackages();
            if (topPackages != null) {
                for (TopPackageDTO pkg : topPackages) {
                    writer.printf("%s,%d,$%.2f,%.1f%%%n",
                            escapeCsv(pkg.getPackageName()),
                            pkg.getSubscriptionCount() != null ? pkg.getSubscriptionCount() : 0,
                            pkg.getRevenue() != null ? pkg.getRevenue() : 0.0,
                            pkg.getRevenuePercentage() != null ? pkg.getRevenuePercentage() : 0.0
                    );
                }
            }
            writer.println();

            // Failed Payments Analysis Section
            writer.println("=== FAILED PAYMENTS ANALYSIS ===");
            writer.println("Reason,Count,Percentage");
            List<FailedPaymentAnalysisDTO> failedPayments = analytics.getFailedPaymentAnalysis();
            if (failedPayments != null) {
                for (FailedPaymentAnalysisDTO item : failedPayments) {
                    writer.printf("%s,%d,%.1f%%%n",
                            escapeCsv(item.getReasonLabel()),
                            item.getCount() != null ? item.getCount() : 0,
                            item.getPercentage() != null ? item.getPercentage() : 0.0
                    );
                }
            }
            writer.println();

            // Payment Success Rate Section
            writer.println("=== PAYMENT SUCCESS RATE ===");
            PaymentSuccessRateDTO successRate = analytics.getPaymentSuccessRate();
            if (successRate != null) {
                writer.printf("Success Rate,%.1f%%%n", successRate.getPaymentSuccessRate() != null ? successRate.getPaymentSuccessRate() : 0.0);
                writer.printf("Total Payments,%d%n", successRate.getTotalPayments() != null ? successRate.getTotalPayments() : 0);
                writer.printf("Successful Payments,%d%n", successRate.getSuccessfulPayments() != null ? successRate.getSuccessfulPayments() : 0);
                writer.printf("Failed Payments,%d%n", successRate.getFailedPayments() != null ? successRate.getFailedPayments() : 0);
            }

            writer.flush();
            return out;

        } catch (Exception e) {
            log.error("Failed to generate analytics CSV file", e);
            throw new RuntimeException("Failed to generate analytics CSV", e);
        }
    }

    /**
     * Generate invoice data as CSV.
     */
    public static ByteArrayOutputStream generateInvoiceCsv(
            String invoiceId,
            String clientName,
            String mspName,
            Instant createdAt,
            Instant paidAt,
            String status,
            double subtotal,
            double discountAmount,
            double discountPercentage,
            String discountType,
            double vatAmount,
            double totalAmount,
            List<ProductSelectionDto> productSelections
    ) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             PrintWriter writer = new PrintWriter(out, true, StandardCharsets.UTF_8)) {

            // Write BOM for UTF-8 to ensure Excel recognizes UTF-8
            out.write(0xEF);
            out.write(0xBB);
            out.write(0xBF);

            // Invoice Header Section
            writer.println("INVOICE DETAILS");
            writer.println();
            writer.printf("Invoice ID,%s%n", escapeCsv(invoiceId));
            writer.printf("Client Name,%s%n", escapeCsv(clientName));
            if (mspName != null && !mspName.isBlank()) {
                writer.printf("MSP Name,%s%n", escapeCsv(mspName));
            }
            String formattedCreatedDate = createdAt != null
                    ? createdAt.atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    : "";
            writer.printf("Date Issued,%s%n", escapeCsv(formattedCreatedDate));
            if (paidAt != null) {
                String formattedPaidDate = paidAt.atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                writer.printf("Date Paid,%s%n", escapeCsv(formattedPaidDate));
            }
            writer.printf("Status,%s%n", escapeCsv(status != null ? status : ""));
            writer.println();

            // Product Details Section
            writer.println("PRODUCT DETAILS");
            writer.println("Product Name,Package Name,Licenses,Price Per License,Validity Period,Validity Unit,Subtotal");
            
            if (productSelections != null && !productSelections.isEmpty()) {
                for (ProductSelectionDto item : productSelections) {
                    int licenseCount = item.getLicenseCount() != null ? item.getLicenseCount() : 0;
                    double pricePerLicense = item.getPricePerLicense() != null ? item.getPricePerLicense() : 0.0;
                    double rowSubtotal = licenseCount * pricePerLicense;
                    String validityUnit = item.getValidityUnit() != null ? item.getValidityUnit().name() : "";
                    int validityPeriod = item.getValidityPeriod() != null ? item.getValidityPeriod() : 0;
                    
                    writer.printf("%s,%s,%d,%.2f,%d,%s,%.2f%n",
                            escapeCsv(item.getProductName()),
                            escapeCsv(item.getPackageName()),
                            licenseCount,
                            pricePerLicense,
                            validityPeriod,
                            escapeCsv(validityUnit),
                            rowSubtotal
                    );
                }
            }
            writer.println();

            // Financial Summary Section
            writer.println("FINANCIAL SUMMARY");
            writer.printf("Subtotal,%.2f%n", subtotal);
            if (discountType != null && !discountType.isBlank()) {
                writer.printf("Discount Type,%s%n", escapeCsv(discountType));
            }
            writer.printf("Discount Amount,%.2f%n", discountAmount);
            if (discountPercentage > 0) {
                writer.printf("Discount Percentage,%.2f%n", discountPercentage);
            }
            writer.printf("VAT Amount,%.2f%n", vatAmount);
            writer.printf("Total Amount,%.2f%n", totalAmount);

            writer.flush();
            return out;

        } catch (Exception e) {
            log.error("Failed to generate invoice CSV file", e);
            throw new RuntimeException("Failed to generate invoice CSV", e);
        }
    }

    /**
     * Generate invoice history data as CSV.
     */
    public static ByteArrayOutputStream generateInvoiceHistoryCsv(List<InvoiceHistoryItemDTO> items) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             PrintWriter writer = new PrintWriter(out, true, StandardCharsets.UTF_8)) {

            // Write BOM for UTF-8 to ensure Excel recognizes UTF-8
            out.write(0xEF);
            out.write(0xBB);
            out.write(0xBF);

            // Header row - Country Name and Coupon Discount Amount moved to end, Country ID removed
            writer.println("SL No,Invoice ID,Invoice Number,Client ID,Client Name,MSP ID,MSP Name,Invoice Date,Paid Date,Status,Role Type,Subtotal,Discount Amount,VAT Amount,Total Amount,Outstanding Amount,Payment Method,Coupon Discount Amount,Country Name");

            // Data rows
            for (int i = 0; i < items.size(); i++) {
                InvoiceHistoryItemDTO item = items.get(i);
                String invoiceDate = item.getInvoiceDate() != null 
                    ? item.getInvoiceDate().atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    : "";
                String paidDate = item.getPaidDate() != null 
                    ? item.getPaidDate().atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    : "";
                String status = item.getStatus() != null ? item.getStatus().name() : "";
                String roleType = item.getRoleType() != null ? item.getRoleType().name() : "";
                String paymentMethod = item.getPaymentMethod() != null ? item.getPaymentMethod().name() : "";
                
                writer.printf("%d,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%.2f,%.2f,%.2f,%.2f,%.2f,%s,%.2f,%s%n",
                    i + 1,
                    escapeCsv(item.getId()),
                    escapeCsv(item.getInvoiceNumber() != null ? item.getInvoiceNumber() : item.getId()),
                    escapeCsv(item.getClientId()),
                    escapeCsv(item.getClientName()),
                    escapeCsv(item.getMspId()),
                    escapeCsv(item.getMspName()),
                    escapeCsv(invoiceDate),
                    escapeCsv(paidDate),
                    escapeCsv(status),
                    escapeCsv(roleType),
                    item.getSubtotal() != null ? item.getSubtotal() : 0.0,
                    item.getDiscountAmount() != null ? item.getDiscountAmount() : 0.0,
                    item.getVatAmount() != null ? item.getVatAmount() : 0.0,
                    item.getTotalAmount() != null ? item.getTotalAmount() : 0.0,
                    item.getOutstandingAmount() != null ? item.getOutstandingAmount() : 0.0,
                    escapeCsv(paymentMethod),
                    item.getCouponDiscountAmount() != null ? item.getCouponDiscountAmount() : 0.0,
                    escapeCsv(item.getCountryName())
                );
            }

            writer.flush();
            return out;

        } catch (Exception e) {
            log.error("Failed to generate invoice history CSV file", e);
            throw new RuntimeException("Failed to generate invoice history CSV", e);
        }
    }

    /**
     * Generate coupon list data as CSV.
     */
    public static ByteArrayOutputStream generateCouponListCsv(List<CouponCreateResponseDTO> coupons) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             PrintWriter writer = new PrintWriter(out, true, StandardCharsets.UTF_8)) {

            // Write BOM for UTF-8 to ensure Excel recognizes UTF-8
            out.write(0xEF);
            out.write(0xBB);
            out.write(0xBF);

            // Header row
            writer.println("SL No,ID,Name,Description,Code,Auto Generated,Type,Value,Currency,Valid From,Valid Until,Usage Limit,Total Used,Min Purchase Amount,Is Active,Is Stackable,Product Restrictions,Geographic Restrictions,QR Code URL,Created By,Created At,Updated At");

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            // Data rows
            for (int i = 0; i < coupons.size(); i++) {
                CouponCreateResponseDTO coupon = coupons.get(i);
                
                // Format dates
                String validFrom = coupon.getValidFrom() != null 
                    ? coupon.getValidFrom().atZone(ZoneId.systemDefault()).format(dateFormatter)
                    : "";
                String validUntil = coupon.getValidUntil() != null 
                    ? coupon.getValidUntil().atZone(ZoneId.systemDefault()).format(dateFormatter)
                    : "";
                String createdAt = coupon.getCreatedAt() != null 
                    ? coupon.getCreatedAt().atZone(ZoneId.systemDefault()).format(dateFormatter)
                    : "";
                String updatedAt = coupon.getUpdatedAt() != null 
                    ? coupon.getUpdatedAt().atZone(ZoneId.systemDefault()).format(dateFormatter)
                    : "";
                
                // Format ProductRestrictions
                String productRestrictions = "";
                if (coupon.getProductRestrictions() != null && !coupon.getProductRestrictions().isEmpty()) {
                    productRestrictions = coupon.getProductRestrictions().stream()
                        .map(restriction -> {
                            String productName = restriction.getProductName() != null && !restriction.getProductName().isEmpty()
                                ? restriction.getProductName()
                                : restriction.getProductId();
                            
                            if (restriction.getPackageId() != null && !restriction.getPackageId().isEmpty()) {
                                String packageName = restriction.getPackageName() != null && !restriction.getPackageName().isEmpty()
                                    ? restriction.getPackageName()
                                    : restriction.getPackageId();
                                return productName + " (" + packageName + ")";
                            } else {
                                return productName;
                            }
                        })
                        .collect(Collectors.joining(", "));
                }
                
                // Format GeographicRestrictions
                String geographicRestrictions = "";
                if (coupon.getGeographicRestrictions() != null && !coupon.getGeographicRestrictions().isEmpty()) {
                    geographicRestrictions = String.join(", ", coupon.getGeographicRestrictions());
                }
                
                writer.printf("%d,%s,%s,%s,%s,%s,%s,%.2f,%s,%s,%s,%d,%d,%.2f,%s,%s,%s,%s,%s,%s,%s,%s%n",
                    i + 1,
                    escapeCsv(coupon.getId()),
                    escapeCsv(coupon.getName()),
                    escapeCsv(coupon.getDescription()),
                    escapeCsv(coupon.getCode()),
                    coupon.isAutoGenerated() ? "Yes" : "No",
                    escapeCsv(coupon.getType()),
                    coupon.getValue() != null ? coupon.getValue() : 0.0,
                    escapeCsv(coupon.getCurrency()),
                    escapeCsv(validFrom),
                    escapeCsv(validUntil),
                    coupon.getUsageLimit() != null ? coupon.getUsageLimit() : 0,
                    coupon.getTotalUsed() != null ? coupon.getTotalUsed() : 0,
                    coupon.getMinPurchaseAmount() != null ? coupon.getMinPurchaseAmount() : 0.0,
                    coupon.isActive() ? "Yes" : "No",
                    coupon.isStackable() ? "Yes" : "No",
                    escapeCsv(productRestrictions),
                    escapeCsv(geographicRestrictions),
                    escapeCsv(coupon.getQrCodeUrl()),
                    escapeCsv(coupon.getCreatedBy()),
                    escapeCsv(createdAt),
                    escapeCsv(updatedAt)
                );
            }

            writer.flush();
            return out;

        } catch (Exception e) {
            log.error("Failed to generate coupon list CSV file", e);
            throw new RuntimeException("Failed to generate coupon list CSV", e);
        }
    }

    /**
     * Generate invoice summary report data as CSV.
     */
    public static ByteArrayOutputStream generateInvoiceSummaryReportCsv(List<InvoiceResponseDTO> items) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             PrintWriter writer = new PrintWriter(out, true, StandardCharsets.UTF_8)) {

            out.write(0xEF);
            out.write(0xBB);
            out.write(0xBF);

            writer.println("SL No,Invoice ID,Client Name,MSP Name,Product Names,Package Names,Invoice Date,Paid Date,Status,Role Type,Subtotal,Discount Amount,Coupon Discount Amount,VAT Amount,Total Amount,Payment Method,Country Name");

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            for (int i = 0; i < items.size(); i++) {
                InvoiceResponseDTO item = items.get(i);
                String invoiceDate = item.getCreatedAt() != null
                        ? item.getCreatedAt().atZone(ZoneId.systemDefault()).format(dateFormatter)
                        : "";
                String paidDate = item.getPaidAt() != null
                        ? item.getPaidAt().atZone(ZoneId.systemDefault()).format(dateFormatter)
                        : "";
                String status = item.getStatus() != null ? item.getStatus().name() : "";
                String roleType = item.getRoleType() != null ? item.getRoleType().name() : "";
                String paymentMethod = item.getPaymentMethod() != null ? item.getPaymentMethod().name() : "";

                String productNames = "";
                String packageNames = "";
                if (item.getProductSelections() != null && !item.getProductSelections().isEmpty()) {
                    productNames = item.getProductSelections().stream()
                            .map(ps -> ps.getProductName() != null ? ps.getProductName() : "")
                            .filter(name -> !name.isEmpty())
                            .collect(Collectors.joining("; "));
                    packageNames = item.getProductSelections().stream()
                            .map(ps -> ps.getPackageName() != null ? ps.getPackageName() : "")
                            .filter(name -> !name.isEmpty())
                            .collect(Collectors.joining("; "));
                }

                writer.printf("%d,%s,%s,%s,%s,%s,%s,%s,%s,%s,%.2f,%.2f,%.2f,%.2f,%.2f,%s,%s%n",
                        i + 1,
                        escapeCsv(item.getId()),
                        escapeCsv(item.getClientName()),
                        escapeCsv(item.getMspName()),
                        escapeCsv(productNames),
                        escapeCsv(packageNames),
                        escapeCsv(invoiceDate),
                        escapeCsv(paidDate),
                        escapeCsv(status),
                        escapeCsv(roleType),
                        item.getSubtotal(),
                        item.getDiscountAmount(),
                        item.getCouponDiscountAmount(),
                        item.getVatAmount(),
                        item.getTotalAmount(),
                        escapeCsv(paymentMethod),
                        escapeCsv(item.getCountryName())
                );
            }

            writer.flush();
            return out;

        } catch (Exception e) {
            log.error("Failed to generate invoice summary report CSV file", e);
            throw new RuntimeException("Failed to generate invoice summary report CSV", e);
        }
    }

    private static String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        // If value contains comma, quote, or newline, wrap in quotes and escape quotes
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
