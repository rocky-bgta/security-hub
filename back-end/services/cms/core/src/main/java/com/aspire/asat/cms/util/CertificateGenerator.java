package com.aspire.asat.cms.util;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.HeadlessException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class CertificateGenerator {

    // Inject template resources from classpath
    @Value("classpath:templates/certificate-template.html")
    private Resource templateResource;
    
    @Value("classpath:templates/certificate-template-dynamic.html")
    private Resource templateResourceDynamic;

    // Loaded HTML templates
    private String htmlTemplate;
    private String htmlTemplateDynamic;

    // Load the templates once, after dependency injection
    @PostConstruct
    private void loadTemplate() {
        try (InputStream in = templateResource.getInputStream()) {
            this.htmlTemplate = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read certificate template", e);
        }
        
        try (InputStream in = templateResourceDynamic.getInputStream()) {
            this.htmlTemplateDynamic = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.warn("Could not read dynamic certificate template, will use static template: {}", e.getMessage());
            this.htmlTemplateDynamic = this.htmlTemplate; // Fallback to static template
        }

        // Enable ImageIO disk caching to reduce heap pressure
        javax.imageio.ImageIO.setUseCache(true);
        
        // Verify font availability on server startup
        verifyFontAvailability();
    }
    
    /**
     * Verify that required fonts are available on the server.
     * Logs warnings if fonts are missing but doesn't fail startup.
     * Handles headless mode gracefully.
     */
    private void verifyFontAvailability() {
        try {
            // Check if running in headless mode
            if (GraphicsEnvironment.isHeadless()) {
                log.info("ℹ️  Running in headless mode. Font verification may be limited.");
            }
            
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            String[] availableFonts = ge.getAvailableFontFamilyNames();
            
            // Check for fonts used in certificate template
            String[] requiredFonts = {
                "Times New Roman",
                "Times New Roman MT", 
                "Liberation Serif",
                "DejaVu Serif",
                "Times"
            };
            
            boolean foundSerifFont = false;
            String foundFontName = null;
            for (String requiredFont : requiredFonts) {
                for (String availableFont : availableFonts) {
                    if (availableFont.equalsIgnoreCase(requiredFont)) {
                        foundFontName = availableFont;
                        foundSerifFont = true;
                        break;
                    }
                }
                if (foundSerifFont) break;
            }
            
            if (!foundSerifFont) {
                log.warn("⚠️  Warning: No preferred serif fonts (Times New Roman, Liberation Serif, DejaVu Serif) found on server. " +
                        "Certificate generation will use system default serif font. " +
                        "Available fonts (first 10): {}", 
                        availableFonts.length > 0 
                            ? String.join(", ", java.util.Arrays.asList(availableFonts).subList(0, Math.min(10, availableFonts.length)))
                            : "none detected");
            } else {
                log.info("✅ Certificate font verification passed. Using font: {} for certificate generation.", foundFontName);
            }
            
        } catch (HeadlessException e) {
            log.info("ℹ️  Headless environment detected. Font verification skipped. " +
                    "PDF generation will use system fonts, and PDFBox will render fonts from embedded PDF.");
        } catch (Exception e) {
            log.warn("⚠️  Could not verify font availability: {}. Certificate generation may use fallback fonts. " +
                    "This is usually fine as fonts are embedded in PDF.", e.getMessage());
        }
    }


    private String populateTemplate(String name, String courseName, String date, String certificateId) {
        return htmlTemplate
                .replace("{{name}}", name)
                .replace("{{courseName}}", courseName)
                .replace("{{date}}", date)
                .replace("{{certificateId}}", certificateId);
    }

    /**
     * Populate template with certificate data and background image URL.
     * Replaces placeholders in the HTML template with actual values.
     *
     * @param htmlTemplate The HTML template string
     * @param name The learner's name
     * @param courseName The course name
     * @param date The certificate issue date
     * @param certificateId The certificate ID
     * @param backgroundImageUrl The background image URL to replace the placeholder
     * @return Populated HTML template
     */
    private String populateTemplate(String htmlTemplate, String name, String courseName, 
                                    String date, String certificateId, String backgroundImageUrl) {
        if (htmlTemplate == null) {
            throw new IllegalArgumentException("HTML template cannot be null");
        }
        
        String populated = htmlTemplate
                .replace("{{name}}", name != null ? name : "")
                .replace("{{courseName}}", courseName != null ? courseName : "")
                .replace("{{date}}", date != null ? date : "")
                .replace("{{certificateId}}", certificateId != null ? certificateId : "");
        
        // Replace background image placeholder with actual URL
        // Support multiple placeholder formats
        if (backgroundImageUrl != null && !backgroundImageUrl.isBlank()) {
            String escapedBackgroundUrl = escapeUrlForXml(backgroundImageUrl);
            populated = populated
                    .replace("{{backgroundImageUrl}}", escapedBackgroundUrl)
                    .replace("{{backgroundImage}}", escapedBackgroundUrl)
                    .replace("{{bgImageUrl}}", escapedBackgroundUrl)
                    .replace("{{bgImage}}", escapedBackgroundUrl);
        } else {
            // Remove placeholder if no background image URL provided
            populated = populated
                    .replace("{{backgroundImageUrl}}", "")
                    .replace("{{backgroundImage}}", "")
                    .replace("{{bgImageUrl}}", "")
                    .replace("{{bgImage}}", "");
        }
        
        return populated;
    }

    /**
     * Populate template with all certificate data including new template fields.
     * Replaces all placeholders in the HTML template with actual values.
     * 
     * IMPORTANT NOTES:
     * 1. S3/CloudFront URLs: The PDF renderer (openhtmltopdf) will fetch images from URLs at render time.
     *    URLs must be publicly accessible or accessible from the server. The renderer does NOT need the
     *    images to be embedded in the HTML - it fetches them from the URLs when generating the PDF.
     * 
     * 2. Missing placeholders: If a placeholder doesn't exist in the template, String.replace() will
     *    simply do nothing (no error). The original template string is returned unchanged for that
     *    placeholder. This is safe and won't cause errors.
     *
     * @param htmlTemplate The HTML template string
     * @param name The learner's name
     * @param courseName The course name
     * @param date The certificate issue date
     * @param certificateId The certificate ID
     * @param certificateTitle The certificate title
     * @param certificateType The certificate type
     * @param acknowledgement The acknowledgement text
     * @param completionStatus The completion status text
     * @param completionTitle The completion title
     * @param logoImageUrl The logo image URL (S3/CloudFront URL - will be fetched by PDF renderer)
     * @param signatureImageUrl The signature image URL (S3/CloudFront URL - will be fetched by PDF renderer)
     * @param signerName The signer's name
     * @param signerDesignation The signer's designation
     * @param signatureIdentity The signature identity
     * @param backgroundImageUrl The background image URL (S3/CloudFront URL - will be fetched by PDF renderer)
     * @return Populated HTML template
     */
    private String populateTemplate(String htmlTemplate, String name, String courseName, 
                                    String date, String certificateId,
                                    String certificateTitle, String certificateType,
                                    String acknowledgement, String completionStatus,
                                    String completionTitle, String logoImageUrl,
                                    String signatureImageUrl, String signerName,
                                    String signerDesignation, String signatureIdentity,
                                    String backgroundImageUrl) {
        if (htmlTemplate == null) {
            throw new IllegalArgumentException("HTML template cannot be null");
        }
        
        // Replace all placeholders with actual values
        // Note: String.replace() is safe - if placeholder doesn't exist, it simply does nothing (no error)
        String populated = htmlTemplate
                .replace("{{name}}", escapeHtml(name != null ? name : ""))
                .replace("{{courseName}}", escapeHtml(courseName != null ? courseName : ""))
                .replace("{{date}}", escapeHtml(date != null ? date : ""))
                .replace("{{certificateId}}", escapeHtml(certificateId != null ? certificateId : ""))
                .replace("{{certificateTitle}}", escapeHtml(certificateTitle != null ? certificateTitle : ""))
                .replace("{{certificateType}}", escapeHtml(certificateType != null ? certificateType : ""))
                .replace("{{acknowledgement}}", escapeHtml(acknowledgement != null ? acknowledgement : ""))
                .replace("{{completionStatus}}", escapeHtml(completionStatus != null ? completionStatus : ""))
                .replace("{{completionTitle}}", escapeHtml(completionTitle != null ? completionTitle : ""))
                .replace("{{signerName}}", escapeHtml(signerName != null ? signerName : ""))
                .replace("{{signerDesignation}}", escapeHtml(signerDesignation != null ? signerDesignation : ""))
                .replace("{{signatureIdentity}}", escapeHtml(signatureIdentity != null ? signatureIdentity : ""));
        
        // Replace image URLs - escape & to &amp; for XML/HTML compatibility (AWS S3 URLs contain & in query params)
        // These URLs will be fetched by the PDF renderer at render time
        populated = populated
                .replace("{{logoImageUrl}}", logoImageUrl != null ? escapeUrlForXml(logoImageUrl) : "")
                .replace("{{signatureImageUrl}}", signatureImageUrl != null ? escapeUrlForXml(signatureImageUrl) : "");
        
        // Replace background image placeholder with actual URL
        // Support multiple placeholder formats
        if (backgroundImageUrl != null && !backgroundImageUrl.isBlank()) {
            String escapedBackgroundUrl = escapeUrlForXml(backgroundImageUrl);
            populated = populated
                    .replace("{{backgroundImageUrl}}", escapedBackgroundUrl)
                    .replace("{{backgroundImage}}", escapedBackgroundUrl)
                    .replace("{{bgImageUrl}}", escapedBackgroundUrl)
                    .replace("{{bgImage}}", escapedBackgroundUrl);
        } else {
            // Remove placeholder if no background image URL provided
            populated = populated
                    .replace("{{backgroundImageUrl}}", "")
                    .replace("{{backgroundImage}}", "")
                    .replace("{{bgImageUrl}}", "")
                    .replace("{{bgImage}}", "");
        }
        
        // Log warning if image URLs are provided but might not be accessible
        if (logoImageUrl != null && !logoImageUrl.isBlank() && !isValidUrl(logoImageUrl)) {
            log.warn("Logo image URL may not be valid or accessible: {}", logoImageUrl);
        }
        if (signatureImageUrl != null && !signatureImageUrl.isBlank() && !isValidUrl(signatureImageUrl)) {
            log.warn("Signature image URL may not be valid or accessible: {}", signatureImageUrl);
        }
        if (backgroundImageUrl != null && !backgroundImageUrl.isBlank() && !isValidUrl(backgroundImageUrl)) {
            log.warn("Background image URL may not be valid or accessible: {}", backgroundImageUrl);
        }
        
        return populated;
    }
    
    /**
     * Escape HTML special characters to prevent XSS and ensure proper rendering.
     * 
     * @param text The text to escape
     * @return Escaped text
     */
    private String escapeHtml(String text) {
        if (text == null) {
            return "";
        }
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
    
    /**
     * Escape URL for XML/HTML compatibility.
     * Escapes & characters to &amp; to prevent XML parsing errors with URLs containing query parameters
     * (e.g., AWS S3 presigned URLs with &X-Amz-Date, &X-Amz-Signature, etc.).
     * Avoids double-escaping if the URL already contains HTML entities.
     * 
     * @param url The URL to escape
     * @return Escaped URL with & replaced by &amp; (except in existing HTML entities)
     */
    private String escapeUrlForXml(String url) {
        if (url == null || url.isBlank()) {
            return "";
        }
        // Replace & with &amp; but avoid double-escaping if already part of HTML entity
        // This handles AWS S3 URLs with query parameters like &X-Amz-Date=...
        // Use regex to replace & only when not followed by known HTML entity patterns
        return url.replaceAll("&(?!(amp|lt|gt|quot|#39|#\\d+);)", "&amp;");
    }
    
    /**
     * Basic URL validation to check if URL format is valid.
     * This doesn't guarantee the URL is accessible, but checks the format.
     * 
     * @param url The URL to validate
     * @return true if URL format appears valid, false otherwise
     */
    private boolean isValidUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        try {
            // Check if it starts with http:// or https://
            String lowerUrl = url.toLowerCase().trim();
            return lowerUrl.startsWith("http://") || lowerUrl.startsWith("https://");
        } catch (Exception e) {
            return false;
        }
    }

    public ByteArrayOutputStream generateCertificatePdf(String name, String courseName, String date, String certificateId) {
        try {
            String html = populateTemplate(name, courseName, date, certificateId);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(outputStream);
            
            // Note: openhtmltopdf automatically uses system fonts available on the server
            // Font fallback chain in template: "Times New Roman MT" -> "Times New Roman" -> 
            // "Liberation Serif" -> "DejaVu Serif" -> "Times" -> "serif" (system default)

            builder.run();
            return outputStream;
        } catch (Exception e) {
            log.error("PDF generation failed for certificate: name={}, course={}, error={}", 
                    name, courseName, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Generate certificate PDF using provided HTML template and background image URL.
     * The background image URL will replace placeholders in the HTML template.
     *
     * @param name The learner's name
     * @param courseName The course name
     * @param date The certificate issue date
     * @param certificateId The certificate ID
     * @param htmlTemplate The HTML template string (should contain placeholders like {{name}}, {{courseName}}, etc.)
     * @param backgroundImageUrl The background image URL to replace placeholder in template
     * @return ByteArrayOutputStream containing the generated PDF, or null if generation fails
     */
    public ByteArrayOutputStream generateCertificatePdf(String name, String courseName, String date, 
                                                       String certificateId, String htmlTemplate, String backgroundImageUrl) {
        try {
            if (htmlTemplate == null || htmlTemplate.isBlank()) {
                log.error("HTML template is null or empty");
                return null;
            }

            String html = populateTemplate(htmlTemplate, name, courseName, date, certificateId, backgroundImageUrl);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(outputStream);
            
            // Note: openhtmltopdf automatically uses system fonts available on the server
            // Font fallback chain in template: "Times New Roman MT" -> "Times New Roman" -> 
            // "Liberation Serif" -> "DejaVu Serif" -> "Times" -> "serif" (system default)

            builder.run();
            log.debug("Successfully generated certificate PDF using custom template for: {}", name);
            return outputStream;
        } catch (Exception e) {
            log.error("PDF generation failed for certificate: name={}, course={}, error={}", 
                    name, courseName, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Generate PNG image from PDF (legacy method using ByteArrayOutputStream).
     * Uses memory-efficient settings and high-quality font rendering.
     * Note: Prefer the stream-based method for better memory management.
     */
    public ByteArrayOutputStream generateCertificateImageFromPdf(ByteArrayOutputStream pdfStream) {
        if (pdfStream == null || pdfStream.size() == 0) {
            log.error("Invalid PDF stream: null or empty");
            return null;
        }
        
        // Use memory-efficient settings similar to stream-based method
        MemoryUsageSetting mus = MemoryUsageSetting.setupMixed(64 * 1024 * 1024);
        
        try (PDDocument document = PDDocument.load(
                new ByteArrayInputStream(pdfStream.toByteArray()), mus)) {
            
            // Check if document has pages
            if (document.getNumberOfPages() == 0) {
                log.error("PDF document has no pages");
                return null;
            }
            
            PDFRenderer renderer = new PDFRenderer(document);
            renderer.setSubsamplingAllowed(false); // Disable subsampling for better font quality
            
            // Use 400 DPI for better font rendering matching PDF quality
            // Certificate dimensions: 1123x794px, at 400 DPI = ~4492x3176px = ~14.3M pixels
            final int TARGET_DPI = 400;
            BufferedImage image = renderer.renderImageWithDPI(0, TARGET_DPI, org.apache.pdfbox.rendering.ImageType.ARGB);
            
            // Check if image was successfully rendered
            if (image == null) {
                log.error("Failed to render PDF page to image");
                return null;
            }
            
            // Validate image size to prevent memory issues
            final long MAX_PIXELS = 16_000_000L;
            long pixelCount = (long) image.getWidth() * (long) image.getHeight();
            if (pixelCount > MAX_PIXELS) {
                log.error("Rendered image too large: {}x{} ({} pixels). Maximum allowed: {}", 
                        image.getWidth(), image.getHeight(), pixelCount, MAX_PIXELS);
                image.flush();
                return null;
            }

            // Apply font quality enhancements
            BufferedImage processedImage = enhanceImageQualityForFonts(image);
            
            ByteArrayOutputStream imageStream = new ByteArrayOutputStream();
            boolean written = ImageIO.write(processedImage, "png", imageStream);
            
            if (!written) {
                log.error("Failed to write PNG image - no suitable writer found");
                processedImage.flush();
                image.flush();
                return null;
            }
            
            processedImage.flush();
            image.flush();

            log.debug("Successfully converted PDF to PNG: {}x{} pixels", 
                    processedImage.getWidth(), processedImage.getHeight());
            return imageStream;
            
        } catch (OutOfMemoryError oom) {
            log.error("Out of memory while rendering certificate image. Consider lowering DPI or increasing Xmx.", oom);
            return null;
        } catch (IOException e) {
            log.error("Failed to convert PDF to PNG image: {}", e.getMessage(), e);
            return null;
        } catch (Exception e) {
            log.error("Unexpected error during PDF to PNG conversion: {}", e.getMessage(), e);
            return null;
        }
    }


    /**
     * NEW: Stream-friendly PDF creation (writes directly to the provided OutputStream).
     * Keeps heap usage low because we don't build a giant byte[] first.
     * Fonts are handled by the HTML template and should be embedded automatically by openhtmltopdf.
     */
    public void generateCertificatePdfToStream(
            String name,
            String courseName,
            String date,
            String certificateId,
            OutputStream out
    ) throws IOException {
        try {
            String html = populateTemplate(name, courseName, date, certificateId);
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(out);          // streams to caller
            
            // Note: openhtmltopdf automatically uses system fonts available on the server
            // Font fallback chain in template ensures proper rendering even if Times New Roman is unavailable
            // The template uses: "Times New Roman MT" -> "Times New Roman" -> "Liberation Serif" -> 
            // "DejaVu Serif" -> "Times" -> "serif" (system default)
            
            builder.run();
            out.flush();
        } catch (Exception e) {
            log.error("PDF generation failed for certificate: name={}, course={}, error={}", 
                    name, courseName, e.getMessage(), e);
            throw new IOException("PDF generation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Stream-friendly PDF creation using provided HTML template and background image URL.
     * Writes directly to the provided OutputStream to keep heap usage low.
     * The background image URL will replace placeholders in the HTML template.
     * Fonts are handled by the HTML template and should be embedded automatically by openhtmltopdf.
     *
     * @param name The learner's name
     * @param courseName The course name
     * @param date The certificate issue date
     * @param certificateId The certificate ID
     * @param htmlTemplate The HTML template string (should contain placeholders like {{name}}, {{courseName}}, etc.)
     * @param backgroundImageUrl The background image URL to replace placeholder in template
     * @param out The output stream to write the PDF to
     * @throws IOException if PDF generation fails
     */
    public void generateCertificatePdfToStream(
            String name,
            String courseName,
            String date,
            String certificateId,
            String htmlTemplate,
            String backgroundImageUrl,
            OutputStream out
    ) throws IOException {
        try {
            if (htmlTemplate == null || htmlTemplate.isBlank()) {
                throw new IllegalArgumentException("HTML template cannot be null or empty");
            }

            String html = populateTemplate(htmlTemplate, name, courseName, date, certificateId, backgroundImageUrl);
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(out);          // streams to caller
            
            // Note: openhtmltopdf automatically uses system fonts available on the server
            // Font fallback chain in template ensures proper rendering even if Times New Roman is unavailable
            // The template uses: "Times New Roman MT" -> "Times New Roman" -> "Liberation Serif" -> 
            // "DejaVu Serif" -> "Times" -> "serif" (system default)
            
            builder.run();
            out.flush();
            log.debug("Successfully generated certificate PDF to stream using custom template for: {}", name);
        } catch (IllegalArgumentException e) {
            log.error("Invalid parameters for PDF generation: {}", e.getMessage());
            throw new IOException("PDF generation failed: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("PDF generation failed for certificate: name={}, course={}, error={}", 
                    name, courseName, e.getMessage(), e);
            throw new IOException("PDF generation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Stream-friendly PDF creation using all certificate template fields.
     * Writes directly to the provided OutputStream to keep heap usage low.
     * All template fields will replace placeholders in the HTML template.
     * Fonts are handled by the HTML template and should be embedded automatically by openhtmltopdf.
     *
     * @param name The learner's name
     * @param courseName The course name
     * @param date The certificate issue date
     * @param certificateId The certificate ID
     * @param certificateTitle The certificate title
     * @param certificateType The certificate type
     * @param acknowledgement The acknowledgement text
     * @param completionStatus The completion status text
     * @param completionTitle The completion title
     * @param logoImageUrl The logo image URL
     * @param signatureImageUrl The signature image URL
     * @param signerName The signer's name
     * @param signerDesignation The signer's designation
     * @param signatureIdentity The signature identity
     * @param backgroundImageUrl The background image URL
     * @param out The output stream to write the PDF to
     * @throws IOException if PDF generation fails
     */
    public void generateCertificatePdfToStream(
            String name,
            String courseName,
            String date,
            String certificateId,
            String certificateTitle,
            String certificateType,
            String acknowledgement,
            String completionStatus,
            String completionTitle,
            String logoImageUrl,
            String signatureImageUrl,
            String signerName,
            String signerDesignation,
            String signatureIdentity,
            String backgroundImageUrl,
            OutputStream out
    ) throws IOException {
        try {
            // Use dynamic template if backgroundImageUrl is provided, otherwise use static template
            String htmlTemplateToUse = (backgroundImageUrl != null && !backgroundImageUrl.isBlank()) 
                    ? htmlTemplateDynamic 
                    : htmlTemplate;
            
            if (htmlTemplateToUse == null || htmlTemplateToUse.isBlank()) {
                throw new IllegalArgumentException("HTML template cannot be null or empty");
            }

            String html = populateTemplate(htmlTemplateToUse, name, courseName, date, certificateId,
                    certificateTitle, certificateType, acknowledgement, completionStatus,
                    completionTitle, logoImageUrl, signatureImageUrl, signerName,
                    signerDesignation, signatureIdentity, backgroundImageUrl);
            
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(out);
            
            builder.run();
            out.flush();
            log.debug("Successfully generated certificate PDF to stream with all template fields for: {}", name);
        } catch (IllegalArgumentException e) {
            log.error("Invalid parameters for PDF generation: {}", e.getMessage());
            throw new IOException("PDF generation failed: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("PDF generation failed for certificate: name={}, course={}, error={}", 
                    name, courseName, e.getMessage(), e);
            throw new IOException("PDF generation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Stream-friendly PNG creation. Uses PDFBox temp-file mode and bounded DPI
     * to avoid big BufferedImages in heap.
     * Improved font rendering by using higher DPI and better rendering hints.
     * Enhanced to match PDF font quality exactly.
     */
    public void generateCertificateImageFromPdf(InputStream pdfIn, OutputStream pngOut) throws IOException {
       MemoryUsageSetting mus = MemoryUsageSetting.setupMixed(64 * 1024 * 1024);

        try (PDDocument document = PDDocument.load(pdfIn, mus)) {
            // Check if document has pages
            if (document.getNumberOfPages() == 0) {
                throw new IOException("PDF document has no pages");
            }

            PDFRenderer renderer = new PDFRenderer(document);
            renderer.setSubsamplingAllowed(false); // Disable subsampling for better font quality

            // Use higher DPI for better font rendering (400 DPI for excellent quality matching PDF)
            // Certificate dimensions: 1123x794px, at 400 DPI = ~4492x3176px = ~14.3M pixels
            final int TARGET_DPI = 400;
            
            // Use ARGB image type for better color accuracy and font rendering
            BufferedImage image = renderer.renderImageWithDPI(0, TARGET_DPI, org.apache.pdfbox.rendering.ImageType.ARGB);

            // Check if image was successfully rendered
            if (image == null) {
                throw new IOException("Failed to render PDF page to image");
            }

            // Increased limit to accommodate 400 DPI rendering (certificate at 400 DPI ≈ 14.3M pixels)
            final long MAX_PIXELS = 16_000_000L;
            long pixelCount = (long) image.getWidth() * (long) image.getHeight();
            if (pixelCount > MAX_PIXELS) {
                int width = image.getWidth();
                int height = image.getHeight();
                image.flush();
                throw new IOException("Rendered image too large: " + width + "x" + height + " (" + pixelCount + " pixels). Maximum allowed: " + MAX_PIXELS);
            }

            // Apply additional image processing for better font clarity matching PDF quality
            BufferedImage processedImage = enhanceImageQualityForFonts(image);

            // Write PNG with high quality (better for text than JPEG)
            boolean written = ImageIO.write(processedImage, "png", pngOut);
            if (!written) {
                processedImage.flush();
                image.flush();
                throw new IOException("Failed to write PNG image - no suitable writer found");
            }
            
            pngOut.flush();
            log.debug("Successfully converted PDF to PNG: {}x{} pixels", 
                    processedImage.getWidth(), processedImage.getHeight());

            processedImage.flush();
            image.flush();
        } catch (OutOfMemoryError oom) {
            throw new IOException("Out of memory while rendering certificate image; lower DPI or increase Xmx.", oom);
        } catch (Exception e) {
            if (e instanceof IOException) {
                throw e;
            }
            throw new IOException("Failed to convert PDF to PNG image", e);
        }
    }

    /**
     * Enhance image quality for better font rendering to match PDF quality exactly.
     * Applies optimal rendering hints specifically for text/font clarity.
     */
    private BufferedImage enhanceImageQualityForFonts(BufferedImage original) {
        // Use ARGB type to preserve alpha channel and color accuracy
        BufferedImage enhanced = new BufferedImage(
            original.getWidth(),
            original.getHeight(),
            BufferedImage.TYPE_INT_ARGB
        );
        
        Graphics2D g2d = enhanced.createGraphics();
        
        // Enable highest quality rendering hints for perfect font rendering matching PDF
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Use LCD text antialiasing for better font clarity (matches PDF rendering)
        // This provides the best text rendering quality, especially for serif fonts like Times New Roman
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        
        // Draw the original image with all quality enhancements
        g2d.drawImage(original, 0, 0, null);
        g2d.dispose();
        
        return enhanced;
    }



}
