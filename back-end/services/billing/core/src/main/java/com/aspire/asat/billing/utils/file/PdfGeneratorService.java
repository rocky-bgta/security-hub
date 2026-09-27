package com.aspire.asat.billing.utils.file;

import com.aspire.asat.billing.dto.analytics.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@Slf4j
public class PdfGeneratorService {

    // Color scheme
    private static final BaseColor PRIMARY_COLOR = new BaseColor(41, 128, 185);   // Blue
    private static final BaseColor HEADER_BG = new BaseColor(52, 73, 94);         // Dark slate
    private static final BaseColor LIGHT_BG = new BaseColor(236, 240, 241);       // Light gray
    private static final BaseColor SUCCESS_COLOR = new BaseColor(39, 174, 96);    // Green
    private static final BaseColor DANGER_COLOR = new BaseColor(231, 76, 60);     // Red

    /**
     * @author Mahadi Hasan Joy
     * @since 2025-04-10
     */
    public String generateInvoicePdf(String invoiceId, Double amount, String clientName,
                                     String paymentMethod, String currency) {
        try {
            String fileName = "Invoice_" + invoiceId + "_" + System.currentTimeMillis() + ".pdf";
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();

            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Font regularFont = new Font(Font.FontFamily.HELVETICA, 12);

            String formattedDate = Instant.now().atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            document.add(new Paragraph("Aspire LMS - Invoice Receipt", titleFont));
            document.add(new Paragraph("Generated Date: " + formattedDate, regularFont));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Invoice ID: " + invoiceId, regularFont));
            document.add(new Paragraph("Client Name: " + clientName, regularFont));
            document.add(new Paragraph("Payment Method: " + paymentMethod, regularFont));
            document.add(new Paragraph("Amount: " + amount + " " + currency, regularFont));

            document.add(new Paragraph("\n\nThank you for your payment!", regularFont));

            document.close();

            return fileName;

        } catch (Exception e) {
            log.error("Failed to generate invoice PDF", e);
            return null;
        }
    }

    public ByteArrayOutputStream generateInvoicePdfStream(String invoiceId, Double amount,
                                                          String clientName, String paymentMethod,
                                                          String currency) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);
            document.open();

            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Font regularFont = new Font(Font.FontFamily.HELVETICA, 12);

            String formattedDate = Instant.now().atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            document.add(new Paragraph("Aspire LMS - Invoice Receipt", titleFont));
            document.add(new Paragraph("Generated Date: " + formattedDate, regularFont));
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Invoice ID: " + invoiceId, regularFont));
            document.add(new Paragraph("Client Name: " + clientName, regularFont));
            document.add(new Paragraph("Payment Method: " + paymentMethod, regularFont));
            document.add(new Paragraph("Amount: " + amount + " " + currency, regularFont));
            document.add(new Paragraph("\n\nThank you for your payment!", regularFont));

            document.close();

            return outputStream;
        } catch (Exception e) {
            log.error("Failed to generate invoice PDF stream", e);
            return null;
        }
    }

    /**
     * Generate analytics report as PDF.
     */
    public ByteArrayOutputStream generateAnalyticsPdf(BillingAnalyticsResponseDTO analytics) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 36, 36, 54, 36);
            PdfWriter.getInstance(document, outputStream);
            document.open();

            // Fonts
            Font titleFont = new Font(Font.FontFamily.HELVETICA, 24, Font.BOLD, PRIMARY_COLOR);
            Font subtitleFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL, BaseColor.GRAY);
            Font sectionFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, HEADER_BG);
            Font headerFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE);
            Font regularFont = new Font(Font.FontFamily.HELVETICA, 10);
            Font boldFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
            Font cardTitleFont = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, BaseColor.GRAY);
            Font cardValueFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, HEADER_BG);

            // Title
            Paragraph title = new Paragraph("Billing Analytics Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            // Date range
            String formattedDate = Instant.now().atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));
            Paragraph dateRange = new Paragraph(
                    String.format("Period: %s | Date Range: %s to %s | Generated: %s",
                            analytics.getPeriod(),
                            analytics.getStartDate() != null ? analytics.getStartDate() : "N/A",
                            analytics.getEndDate() != null ? analytics.getEndDate() : "N/A",
                            formattedDate),
                    subtitleFont
            );
            dateRange.setAlignment(Element.ALIGN_CENTER);
            dateRange.setSpacingAfter(20);
            document.add(dateRange);

            // Summary Section
            addSectionTitle(document, "Summary Overview", sectionFont);
            addSummaryCards(document, analytics.getSummary(), cardTitleFont, cardValueFont);

            // Revenue Trend Section
            document.add(new Paragraph(" "));
            addSectionTitle(document, "Revenue Trend", sectionFont);
            addRevenueTrendTable(document, analytics.getRevenueTrend(), headerFont, regularFont);

            // Top Packages Section
            document.add(new Paragraph(" "));
            addSectionTitle(document, "Top Performing Packages", sectionFont);
            addTopPackagesTable(document, analytics.getTopPackages(), headerFont, regularFont);

            // Failed Payments Analysis Section
            document.add(new Paragraph(" "));
            addSectionTitle(document, "Failed Payments Analysis", sectionFont);
            addFailedPaymentsTable(document, analytics.getFailedPaymentAnalysis(), headerFont, regularFont);

            // Payment Success Rate Section
            document.add(new Paragraph(" "));
            addSectionTitle(document, "Payment Success Rate", sectionFont);
            addPaymentSuccessRate(document, analytics.getPaymentSuccessRate(), cardTitleFont, cardValueFont);

            document.close();
            return outputStream;

        } catch (Exception e) {
            log.error("Failed to generate analytics PDF", e);
            throw new RuntimeException("Failed to generate analytics PDF", e);
        }
    }

    private void addSectionTitle(Document document, String title, Font font) throws DocumentException {
        Paragraph section = new Paragraph(title, font);
        section.setSpacingBefore(15);
        section.setSpacingAfter(10);
        document.add(section);
    }

    private void addSummaryCards(Document document, BillingAnalyticsSummaryDTO summary,
                                  Font titleFont, Font valueFont) throws DocumentException {
        if (summary == null) return;

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setSpacingAfter(10);

        // Total Revenue
        addSummaryCard(table, "Total Revenue",
                String.format("$%.2f", summary.getTotalRevenue() != null ? summary.getTotalRevenue() : 0.0),
                String.format("%+.1f%%", summary.getTotalRevenueChangePercent() != null ? summary.getTotalRevenueChangePercent() : 0.0),
                titleFont, valueFont);

        // Total Refunds
        addSummaryCard(table, "Total Refunds",
                String.format("$%.2f", summary.getTotalRefunds() != null ? summary.getTotalRefunds() : 0.0),
                String.format("%.1f%% rate", summary.getRefundRate() != null ? summary.getRefundRate() : 0.0),
                titleFont, valueFont);

        // New Subscriptions
        addSummaryCard(table, "New Subscriptions",
                String.valueOf(summary.getNewSubscriptions() != null ? summary.getNewSubscriptions() : 0),
                String.format("%+.1f%%", summary.getNewSubscriptionsChangePercent() != null ? summary.getNewSubscriptionsChangePercent() : 0.0),
                titleFont, valueFont);

        // Failed Payments
        addSummaryCard(table, "Failed Payments",
                String.valueOf(summary.getFailedPaymentsCount() != null ? summary.getFailedPaymentsCount() : 0),
                summary.getFailedPaymentsLabel() != null ? summary.getFailedPaymentsLabel() : "",
                titleFont, valueFont);

        document.add(table);
    }

    private void addSummaryCard(PdfPTable table, String title, String value, String subtitle,
                                 Font titleFont, Font valueFont) {
        PdfPCell cell = new PdfPCell();
        cell.setBorderColor(LIGHT_BG);
        cell.setBackgroundColor(BaseColor.WHITE);
        cell.setPadding(10);

        Paragraph p = new Paragraph();
        p.add(new Chunk(title + "\n", titleFont));
        p.add(new Chunk(value + "\n", valueFont));
        p.add(new Chunk(subtitle, new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL,
                subtitle.contains("+") ? SUCCESS_COLOR : (subtitle.contains("-") ? DANGER_COLOR : BaseColor.GRAY))));

        cell.addElement(p);
        table.addCell(cell);
    }

    private void addRevenueTrendTable(Document document, List<RevenueByPeriodDTO> data,
                                       Font headerFont, Font regularFont) throws DocumentException {
        if (data == null || data.isEmpty()) {
            document.add(new Paragraph("No revenue data available.", regularFont));
            return;
        }

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2, 2, 2, 2, 2});

        // Headers
        addTableHeader(table, "Period", headerFont);
        addTableHeader(table, "Revenue", headerFont);
        addTableHeader(table, "New Subs", headerFont);
        addTableHeader(table, "Refunds", headerFont);
        addTableHeader(table, "Net Revenue", headerFont);

        // Data rows
        boolean alternate = false;
        for (RevenueByPeriodDTO item : data) {
            addTableCell(table, item.getPeriod(), regularFont, alternate);
            addTableCell(table, String.format("$%.2f", item.getRevenue() != null ? item.getRevenue() : 0.0), regularFont, alternate);
            addTableCell(table, String.valueOf(item.getNewSubscriptions() != null ? item.getNewSubscriptions() : 0), regularFont, alternate);
            addTableCell(table, String.format("$%.2f", item.getRefunds() != null ? item.getRefunds() : 0.0), regularFont, alternate);
            addTableCell(table, String.format("$%.2f", item.getNetRevenue() != null ? item.getNetRevenue() : 0.0), regularFont, alternate);
            alternate = !alternate;
        }

        document.add(table);
    }

    private void addTopPackagesTable(Document document, List<TopPackageDTO> data,
                                      Font headerFont, Font regularFont) throws DocumentException {
        if (data == null || data.isEmpty()) {
            document.add(new Paragraph("No package data available.", regularFont));
            return;
        }

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{3, 2, 2, 2});

        // Headers
        addTableHeader(table, "Package Name", headerFont);
        addTableHeader(table, "Subscriptions", headerFont);
        addTableHeader(table, "Revenue", headerFont);
        addTableHeader(table, "Revenue %", headerFont);

        // Data rows
        boolean alternate = false;
        for (TopPackageDTO item : data) {
            addTableCell(table, item.getPackageName(), regularFont, alternate);
            addTableCell(table, String.valueOf(item.getSubscriptionCount() != null ? item.getSubscriptionCount() : 0), regularFont, alternate);
            addTableCell(table, String.format("$%.2f", item.getRevenue() != null ? item.getRevenue() : 0.0), regularFont, alternate);
            addTableCell(table, String.format("%.1f%%", item.getRevenuePercentage() != null ? item.getRevenuePercentage() : 0.0), regularFont, alternate);
            alternate = !alternate;
        }

        document.add(table);
    }

    private void addFailedPaymentsTable(Document document, List<FailedPaymentAnalysisDTO> data,
                                         Font headerFont, Font regularFont) throws DocumentException {
        if (data == null || data.isEmpty()) {
            document.add(new Paragraph("No failed payments data available.", regularFont));
            return;
        }

        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{4, 2, 2});

        // Headers
        addTableHeader(table, "Failure Reason", headerFont);
        addTableHeader(table, "Count", headerFont);
        addTableHeader(table, "Percentage", headerFont);

        // Data rows
        boolean alternate = false;
        for (FailedPaymentAnalysisDTO item : data) {
            addTableCell(table, item.getReasonLabel(), regularFont, alternate);
            addTableCell(table, String.valueOf(item.getCount() != null ? item.getCount() : 0), regularFont, alternate);
            addTableCell(table, String.format("%.1f%%", item.getPercentage() != null ? item.getPercentage() : 0.0), regularFont, alternate);
            alternate = !alternate;
        }

        document.add(table);
    }

    private void addPaymentSuccessRate(Document document, PaymentSuccessRateDTO data,
                                        Font titleFont, Font valueFont) throws DocumentException {
        if (data == null) {
            document.add(new Paragraph("No payment success data available.",
                    new Font(Font.FontFamily.HELVETICA, 10)));
            return;
        }

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setSpacingAfter(10);

        // Success Rate
        addSummaryCard(table, "Success Rate",
                String.format("%.1f%%", data.getPaymentSuccessRate() != null ? data.getPaymentSuccessRate() : 0.0),
                "of all payments",
                titleFont, valueFont);

        // Total Payments
        addSummaryCard(table, "Total Payments",
                String.valueOf(data.getTotalPayments() != null ? data.getTotalPayments() : 0),
                "payments processed",
                titleFont, valueFont);

        // Successful Payments
        addSummaryCard(table, "Successful",
                String.valueOf(data.getSuccessfulPayments() != null ? data.getSuccessfulPayments() : 0),
                "completed",
                titleFont, valueFont);

        // Failed Payments
        addSummaryCard(table, "Failed",
                String.valueOf(data.getFailedPayments() != null ? data.getFailedPayments() : 0),
                "need attention",
                titleFont, valueFont);

        document.add(table);
    }

    private void addTableHeader(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(HEADER_BG);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(8);
        table.addCell(cell);
    }

    private void addTableCell(PdfPTable table, String text, Font font, boolean alternate) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setBackgroundColor(alternate ? LIGHT_BG : BaseColor.WHITE);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setPadding(6);
        table.addCell(cell);
    }
}
