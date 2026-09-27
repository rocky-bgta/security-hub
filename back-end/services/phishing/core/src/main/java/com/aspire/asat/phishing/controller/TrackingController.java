package com.aspire.asat.phishing.controller;

import com.aspire.asat.phishing.constant.WebApiUrlConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * Public (unauthenticated) endpoints for tracking phishing campaign interactions.
 * These are hit by email clients loading pixels, browsers following links,
 * and users submitting forms on landing pages.
 */
@Tag(name = "Tracking", description = "Phishing campaign tracking endpoints (public, no auth)")
@RequestMapping(value = WebApiUrlConstants.TRACKING_BASE_PATH)
public interface TrackingController {

    @Operation(summary = "Track email open",
               description = "Invisible 1x1 tracking pixel loaded by the email client")
    @GetMapping(value = WebApiUrlConstants.TRACKING_OPEN, produces = "image/gif")
    ResponseEntity<byte[]> trackOpen(
            @PathVariable String trackingId,
            HttpServletRequest request);

    @Operation(summary = "Track link click",
               description = "Records a click event then 302-redirects to the original URL")
    @GetMapping(value = WebApiUrlConstants.TRACKING_CLICK)
    ResponseEntity<Void> trackClick(
            @PathVariable String trackingId,
            @RequestParam("url") String url,
            HttpServletRequest request);

    @Operation(summary = "Serve phishing landing page",
               description = "Records a click event and serves the campaign landing page with form actions rewritten")
    @GetMapping(value = WebApiUrlConstants.TRACKING_PHISH, produces = "text/html")
    ResponseEntity<String> trackPhish(
            @PathVariable String trackingId,
            HttpServletRequest request);

    @Operation(summary = "Redirect shortened SMS link",
               description = "Resolves a per-recipient short code and 302-redirects to the original /t/phish landing URL")
    @GetMapping(value = WebApiUrlConstants.TRACKING_SHORT)
    ResponseEntity<String> redirectShortUrl(
            @PathVariable String shortCode);

    @Operation(summary = "Capture form submission",
               description = "Records submitted data, shows awareness message, then redirects to the landing page redirect URL")
    @PostMapping(value = WebApiUrlConstants.TRACKING_SUBMIT, produces = MediaType.TEXT_HTML_VALUE)
    ResponseEntity<String> trackSubmit(
            @PathVariable String trackingId,
            @RequestParam Map<String, String> formData,
            HttpServletRequest request);

    @Operation(summary = "Report phishing email",
               description = "Records that the recipient reported the email as phishing")
    @GetMapping(value = WebApiUrlConstants.TRACKING_REPORT, produces = "text/html")
    ResponseEntity<String> trackReport(
            @PathVariable String trackingId,
            HttpServletRequest request);

    @Operation(summary = "Report phishing email from Outlook add-in",
               description = "Records report action with optional add-in metadata payload")
    @PostMapping(value = WebApiUrlConstants.TRACKING_REPORT_ADDIN, consumes = "application/json", produces = "application/json")
    ResponseEntity<Map<String, Object>> trackReportFromAddIn(
            @PathVariable String trackingId,
            @RequestBody(required = false) Map<String, Object> payload,
            HttpServletRequest request);
}
