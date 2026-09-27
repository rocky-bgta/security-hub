package com.aspire.asat.common.util;

import com.aspire.asat.common.dto.ProductSelectionDto;
import com.aspire.asat.common.enums.InvoiceStatus;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
@Primary
public class InvoiceGenerator {

    @Value("classpath:templates/invoice_exp.html")
    private Resource invoiceTemplate;

    @Value("classpath:templates/Header.png")
    private Resource headerImage;

    @Value("classpath:templates/Footer.png")
    private Resource footerImage;

    private String htmlTemplate;
    private String headerBase64;
    private String footerBase64;

    @PostConstruct
    private void loadTemplate() {
        try (InputStream in = invoiceTemplate.getInputStream()) {
            this.htmlTemplate = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read invoice template", e);
        }

        // Load header image as base64
        try (InputStream in = headerImage.getInputStream()) {
            byte[] imageBytes = in.readAllBytes();
            this.headerBase64 = "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
            log.info("Header image loaded successfully");
        } catch (IOException e) {
            log.error("Could not read header image", e);
            this.headerBase64 = "";
        }

        // Load footer image as base64
        try (InputStream in = footerImage.getInputStream()) {
            byte[] imageBytes = in.readAllBytes();
            this.footerBase64 = "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
            log.info("Footer image loaded successfully");
        } catch (IOException e) {
            log.error("Could not read footer image", e);
            this.footerBase64 = "";
        }
    }

    public ByteArrayOutputStream generateInvoicePdf(
            String invoiceId,
            String clientName,
            double subtotal,
            double couponDiscount,
            double discount,
            double vatPercentage,
            double vat,
            double total,
            Instant date,
            List<ProductSelectionDto> productSelections,
            InvoiceStatus status,
            Instant expireDate
    ) {
        try {
            String formattedDate = date != null
                    ? date.atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))
                    : "";
            String formattedExpireDate = expireDate != null
                    ? expireDate.atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))
                    : "";

            StringBuilder productRows = new StringBuilder();
            for (ProductSelectionDto item : productSelections) {
                int licenseCount = item.getLicenseCount() != null ? item.getLicenseCount() : 0;
                double pricePerLicense = item.getPricePerLicense() != null ? item.getPricePerLicense() : 0.0;
                double rowSubtotal = licenseCount * pricePerLicense;
                String validity = escapeHtml(item.getValidityPeriod() + " " +
                        (item.getValidityUnit() != null ? item.getValidityUnit().name() : ""));

                productRows.append("<tr>")
                        .append("<td>").append(escapeHtml(item.getProductName())).append("</td>")
                        .append("<td>").append(escapeHtml(item.getPackageName())).append("</td>")
                        .append("<td>").append(licenseCount).append("</td>")
                        .append("<td>$").append(String.format("%.2f", pricePerLicense)).append("</td>")
                        .append("<td>").append(validity).append("</td>")
                        .append("<td>$").append(String.format("%.2f", rowSubtotal)).append("</td>")
                        .append("</tr>");
            }

            String statusText = status != null ? status.name() : "PENDING";
            String totalInWords = escapeHtml(AmountInWordsConverter.toUsdAmountInWords(total));

            // Build coupon discount row conditionally
            String couponDiscountRow = "";
            if (couponDiscount > 0) {
                couponDiscountRow = "<tr><td>Coupon Discount:</td><td>$" + 
                    String.format("%.2f", couponDiscount) + "</td></tr>";
            }
            
            String html = htmlTemplate
                    .replace("{{invoiceId}}", escapeHtml(invoiceId))
                    .replace("{{clientName}}", escapeHtml(clientName))
                    .replace("{{date}}", escapeHtml(formattedDate))
                    .replace("{{expireDate}}", escapeHtml(formattedExpireDate))
                    .replace("{{status}}", escapeHtml(statusText))
                    .replace("{{productRows}}", productRows.toString()) // DO NOT escape!
                    .replace("{{subtotal}}", String.format("%.2f", subtotal))
                    .replace("{{couponDiscountRow}}", couponDiscountRow) // Conditional coupon discount row
                    .replace("{{discount}}", String.format("%.2f", discount))
                    .replace("{{vat}}", String.format("%.2f", vat))
                    .replace("{{total}}", String.format("%.2f", total))
                    .replace("{{totalInWords}}", totalInWords)
                    .replace("Header.png", headerBase64)
                    .replace("Footer.png", footerBase64);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(outputStream);
            builder.run();

            return outputStream;
        } catch (Exception e) {
            log.error("❌ Failed to generate invoice PDF", e);
            return null;
        }
    }

    /**
     * Simple overloaded method for basic invoice PDF generation (used by registration module).
     * This method generates a simpler invoice PDF without detailed product rows.
     *
     * @param invoiceId   The invoice ID
     * @param clientName  The client name
     * @param total       The total amount
     * @param date        The invoice date
     * @return ByteArrayOutputStream containing the PDF
     */
    public ByteArrayOutputStream generateInvoicePdf(String invoiceId, String clientName, double total, Date date) {
        try {
            String formattedDate = new SimpleDateFormat("MMMM d, yyyy").format(date);
            String totalInWords = escapeHtml(AmountInWordsConverter.toUsdAmountInWords(total));
            String html = htmlTemplate
                    .replace("{{invoiceId}}", escapeHtml(invoiceId))
                    .replace("{{clientName}}", escapeHtml(clientName))
                    .replace("{{total}}", String.format("%.2f", total))
                    .replace("{{date}}", escapeHtml(formattedDate))
                    .replace("{{expireDate}}", "")
                    .replace("{{status}}", "PENDING")
                    .replace("{{productRows}}", "")
                    .replace("{{subtotal}}", String.format("%.2f", total))
                    .replace("{{couponDiscountRow}}", "") // No coupon discount in simple invoice
                    .replace("{{discount}}", "0.00")
                    .replace("{{vat}}", "0.00")
                    .replace("{{totalInWords}}", totalInWords)
                    .replace("Header.png", headerBase64)
                    .replace("Footer.png", footerBase64);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(outputStream);
            builder.run();

            return outputStream;
        } catch (Exception e) {
            log.error("❌ Failed to generate simple invoice PDF", e);
            return null;
        }
    }

    private String escapeHtml(String input) {
        return input == null ? "" : input
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
