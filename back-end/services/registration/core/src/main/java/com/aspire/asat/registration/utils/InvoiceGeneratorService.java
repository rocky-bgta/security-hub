package com.aspire.asat.registration.utils;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
@Slf4j
public class InvoiceGeneratorService {

    @Value("classpath:templates/invoice-template.html") // put your HTML template in resources/templates
    private Resource invoiceTemplate;

    private String htmlTemplate;

    @PostConstruct
    private void loadTemplate() {
        try (InputStream in = invoiceTemplate.getInputStream()) {
            this.htmlTemplate = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read invoice template", e);
        }
    }

    public ByteArrayOutputStream generateInvoicePdf(String invoiceId, String clientName, double total, Date date) {
        try {
            String formattedDate = new SimpleDateFormat("yyyy-MM-dd").format(date);
            String html = htmlTemplate
                    .replace("{{invoiceId}}", invoiceId)
                    .replace("{{clientName}}", clientName)
                    .replace("{{total}}", String.format("%.2f", total))
                    .replace("{{date}}", formattedDate);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(outputStream);
            builder.run();

            return outputStream;
        } catch (Exception e) {
            log.error("❌ 3 Failed to generate invoice PDF", e);
            return null;
        }
    }
}
