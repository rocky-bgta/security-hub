package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.dto.request.ImportSiteRequest;
import com.aspire.asat.phishing.dto.enums.ImportStatus;
import com.aspire.asat.phishing.dto.response.ImportedSiteDto;
import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.service.HtmlSanitizerService;
import com.aspire.asat.phishing.service.HtmlValidatorService;
import com.aspire.asat.phishing.service.WebsiteImportService;
import com.aspire.asat.phishing.service.impl.importer.BrowserCaptureClient;
import com.aspire.asat.phishing.service.impl.importer.BrowserHeaderProfile;
import com.aspire.asat.phishing.service.impl.importer.CookieJar;
import com.aspire.asat.phishing.service.impl.importer.ImportBlockDetector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import java.net.HttpURLConnection;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.util.Base64;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Robust website import service modelled after Gophish's /api/import/site.
 *
 * Core behaviour ported from Go:
 *   1. SSRF protection — block connections to internal/link-local/loopback IPs.
 *   2. Fetch with relaxed TLS (many targets use self-signed certs).
 *   3. Inject {@code <base href>} so relative resources (images, CSS, JS) resolve
 *      against the original site.
 *   4. For every {@code <form>}, store the original action URL in a hidden field
 *      ({@code __original_url}) — matches Gophish behaviour exactly.
 *   5. Sanitize dangerous elements (scripts, event handlers) while preserving
 *      the structural/style elements needed for a realistic landing page.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WebsiteImportServiceImpl implements WebsiteImportService {

    private final HtmlSanitizerService htmlSanitizerService;
    private final HtmlValidatorService htmlValidatorService;
    private final ImportBlockDetector importBlockDetector;
    private final BrowserCaptureClient browserCaptureClient;

    private static final int CONNECT_TIMEOUT_MS = 15_000;
    private static final int MAX_BODY_BYTES     = 10 * 1024 * 1024; // 10 MB cap
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
            + "(KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36";

    private static final String[] DENIED_IP_PREFIXES = {
            "0.", "10.", "100.64.", "100.65.", "100.66.", "100.67.",
            "127.", "169.254.", "172.16.", "172.17.", "172.18.", "172.19.",
            "172.20.", "172.21.", "172.22.", "172.23.", "172.24.", "172.25.",
            "172.26.", "172.27.", "172.28.", "172.29.", "172.30.", "172.31.",
            "192.168.", "198.51.100.", "203.0.113.", "224.", "240.", "255.255.255.255"
    };

    private static final Pattern CSS_URL_PATTERN = Pattern.compile("url\\((['\"]?)([^)'\"]+)\\1\\)");
    private static final Pattern CSS_IMPORT_PATTERN = Pattern.compile("@import\\s+(?:url\\()?['\"]?([^'\"\\)]+)['\"]?\\)?");

    @Value("${landing.import.max-http-attempts:3}")
    private int maxHttpAttempts;

    @Value("${landing.import.max-redirects:10}")
    private int maxRedirects;

    @Value("${landing.import.asset.max-count:120}")
    private int maxAssetCount;

    @Value("${landing.import.asset.max-bytes:1048576}")
    private int maxAssetBytes;

    @Value("${landing.import.asset.total-byte-budget:15728640}")
    private int totalAssetByteBudget;

    // ──────────────────────────────────────────────────────────────────────
    //  Public API
    // ──────────────────────────────────────────────────────────────────────

    @Override
    public ImportedSiteDto importWebsite(ImportSiteRequest request) {
        String websiteUrl = normalizeUrl(request.getWebsiteUrl());
        String importId = UUID.randomUUID().toString();
        log.info("Importing website from URL: {} importId={}", websiteUrl, importId);

        validateUrlScheme(websiteUrl);
        blockSsrf(websiteUrl);

        try {
            List<String> warnings = new ArrayList<>();
            FetchOutcome fetchOutcome = fetchWithOrchestrator(websiteUrl, warnings);
            Document doc = fetchOutcome.document();
            doc.outputSettings().prettyPrint(false);

            String pageTitle    = doc.title();
            String metaDesc     = extractMetaDescription(doc);
            List<String> assets = extractAssets(doc, websiteUrl);

            AssetLocalizationSummary localizationSummary = AssetLocalizationSummary.empty();
            if (request.isIncludeAssets()) {
                int safeDepth = Math.max(0, request.getCrawlDepth());
                localizationSummary = localizeAssets(doc, websiteUrl, fetchOutcome.cookieJar(), safeDepth, warnings);
            }

            injectBaseHref(doc, websiteUrl);
            processFormActions(doc, websiteUrl);

            String processedHtml = doc.html();
            //String sanitizedHtml = htmlSanitizerService.sanitize(processedHtml);
            String sanitizedHtml = ensureBaseHref(processedHtml, websiteUrl);

            List<String> formFields = htmlValidatorService.extractFormFields(sanitizedHtml);
            boolean hasLoginForm    = htmlValidatorService.hasLoginForm(sanitizedHtml);

            ImportStatus status = determineImportStatus(fetchOutcome, localizationSummary);

            log.info("Website imported successfully: {} (title={}, forms={}, assets={}, localized={}) importId={}",
                    websiteUrl, pageTitle, hasLoginForm, assets.size(), localizationSummary.localizedCount(), importId);

            return ImportedSiteDto.builder()
                    .htmlContent(sanitizedHtml)
                    .originalUrl(websiteUrl)
                    .pageTitle(pageTitle)
                    .metaDescription(metaDesc)
                    .extractedAssets(assets)
                    .detectedFormFields(formFields)
                    .hasLoginForm(hasLoginForm)
                    .importStatus(status)
                    .importMode(fetchOutcome.mode())
                    .fetchStatusCode(fetchOutcome.statusCode())
                    .finalResolvedUrl(fetchOutcome.finalUrl())
                    .blockedReason(fetchOutcome.blockedReason())
                    .blockedSignals(fetchOutcome.blockedSignals())
                    .assetsFound(localizationSummary.foundCount())
                    .assetsLocalized(localizationSummary.localizedCount())
                    .assetsFailed(localizationSummary.failedCount())
                    .warnings(warnings)
                    .build();

        } catch (ServiceException e) {
            throw e;
        } catch (org.jsoup.HttpStatusException e) {
            log.error("HTTP error importing {}: status={}", websiteUrl, e.getStatusCode(), e);
            throw new ServiceException(
                    "Remote server returned HTTP " + e.getStatusCode(),
                    HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            log.error("Failed to import website {}: {}", websiteUrl, e.getMessage(), e);
            throw new ServiceException(
                    "Failed to fetch website: " + e.getMessage(),
                    HttpStatus.BAD_REQUEST);
        }
    }

    private ImportStatus determineImportStatus(FetchOutcome fetchOutcome, AssetLocalizationSummary summary) {
        if (fetchOutcome.blockedReason() != null) {
            if ("waf_challenge".equals(fetchOutcome.blockedReason()) || "challenge_detected".equals(fetchOutcome.blockedReason())) {
                return ImportStatus.BLOCKED_WAF;
            }
            if ("bot_or_human_challenge".equals(fetchOutcome.blockedReason())) {
                return ImportStatus.BLOCKED_GEO_OR_BOT;
            }
        }
        if (summary.foundCount() > 0 && summary.failedCount() > 0) {
            return ImportStatus.SUCCESS_PARTIAL;
        }
        return ImportStatus.SUCCESS_FULL;
    }

    private FetchOutcome fetchWithOrchestrator(String url, List<String> warnings) throws IOException {
        List<BrowserHeaderProfile> profiles = List.of(
                BrowserHeaderProfile.chromeWindows(),
                BrowserHeaderProfile.edgeWindows(),
                BrowserHeaderProfile.firefox()
        );

        int attempts = Math.min(Math.max(1, maxHttpAttempts), profiles.size());
        FetchOutcome lastOutcome = null;

        for (int i = 0; i < attempts; i++) {
            BrowserHeaderProfile profile = profiles.get(i);
            FetchOutcome outcome = fetchWithProfileAndCookies(url, profile);
            lastOutcome = outcome;
            if (outcome.document() != null && !outcome.blocked()) {
                return outcome;
            }
            warnings.add("HTTP profile attempt failed: " + profile.name() + " status=" + outcome.statusCode());
        }

        BrowserCaptureClient.BrowserCaptureResult browserCapture = browserCaptureClient.capture(url, true, true);
        if (browserCapture.success()) {
            Document document = Jsoup.parse(browserCapture.html());
            return new FetchOutcome(
                    document,
                    browserCapture.finalUrl() != null ? browserCapture.finalUrl() : url,
                    200,
                    "BROWSER_FALLBACK",
                    false,
                    null,
                    Collections.emptyList(),
                    new CookieJar()
            );
        }
        warnings.add("Browser fallback unavailable/failure: " + browserCapture.errorReason());

        if (lastOutcome != null && lastOutcome.statusCode() > 0) {
            throw new org.jsoup.HttpStatusException("HTTP error fetching URL", lastOutcome.statusCode(), url);
        }
        throw new IOException("Failed to fetch website via HTTP profiles and browser fallback");
    }

    private FetchOutcome fetchWithProfileAndCookies(String originalUrl, BrowserHeaderProfile profile) throws IOException {
        CookieJar cookieJar = new CookieJar();
        String rootUrl = toRootUrl(originalUrl);
        if (!rootUrl.equalsIgnoreCase(originalUrl)) {
            safeFetchWithManualRedirect(rootUrl, profile, cookieJar);
        }
        return safeFetchWithManualRedirect(originalUrl, profile, cookieJar);
    }

    private FetchOutcome safeFetchWithManualRedirect(String url, BrowserHeaderProfile profile, CookieJar cookieJar) throws IOException {
        String currentUrl = url;
        int lastStatus = 0;
        for (int i = 0; i < Math.max(1, maxRedirects); i++) {
            blockSsrf(currentUrl);
            Connection.Response response = buildProfileConnection(currentUrl, profile)
                    .followRedirects(false)
                    .cookies(parseCookieHeader(cookieJar.asHeader()))
                    .execute();
            cookieJar.capture(response.multiHeaders().getOrDefault("Set-Cookie", List.of()));
            lastStatus = response.statusCode();

            String location = response.header("Location");
            if (isRedirectStatus(lastStatus) && location != null && !location.isBlank()) {
                currentUrl = new URL(new URL(currentUrl), location).toExternalForm();
                continue;
            }

            if (isSuccess(lastStatus)) {
                Document document = response.parse();
                ImportBlockDetector.DetectionResult detection = importBlockDetector.detect(document.html(), document.title(), lastStatus);
                return new FetchOutcome(document, currentUrl, lastStatus, "HTTP_PROFILE",
                        detection.isBlocked(), detection.getReason(), detection.getSignals(), cookieJar);
            }

            ImportBlockDetector.DetectionResult detection = importBlockDetector.detect(response.body(), "", lastStatus);
            return new FetchOutcome(null, currentUrl, lastStatus, "HTTP_PROFILE",
                    detection.isBlocked(), detection.getReason(), detection.getSignals(), cookieJar);
        }
        return new FetchOutcome(null, currentUrl, lastStatus, "HTTP_PROFILE", true,
                "redirect_limit_exceeded", List.of("redirect_limit_exceeded"), cookieJar);
    }

    private boolean isRedirectStatus(int status) {
        return status == HttpURLConnection.HTTP_MOVED_PERM
                || status == HttpURLConnection.HTTP_MOVED_TEMP
                || status == HttpURLConnection.HTTP_SEE_OTHER
                || status == 307
                || status == 308;
    }

    private String toRootUrl(String url) {
        try {
            URI uri = URI.create(url);
            return uri.getScheme() + "://" + uri.getHost() + "/";
        } catch (Exception e) {
            return url;
        }
    }

    private Connection buildProfileConnection(String url, BrowserHeaderProfile profile) {
        Connection connection = Jsoup.connect(url)
                .timeout(CONNECT_TIMEOUT_MS)
                .maxBodySize(MAX_BODY_BYTES)
                .ignoreHttpErrors(true)
                .sslSocketFactory(InsecureSslSocketFactory.create())
                .method(Connection.Method.GET);
        for (Map.Entry<String, String> header : profile.headers().entrySet()) {
            connection.header(header.getKey(), header.getValue());
        }
        return connection;
    }

    private Map<String, String> parseCookieHeader(String cookieHeader) {
        if (cookieHeader == null || cookieHeader.isBlank()) {
            return new HashMap<>();
        }
        Map<String, String> map = new HashMap<>();
        for (String token : cookieHeader.split(";")) {
            String trimmed = token.trim();
            int idx = trimmed.indexOf('=');
            if (idx > 0) {
                map.put(trimmed.substring(0, idx).trim(), trimmed.substring(idx + 1).trim());
            }
        }
        return map;
    }

    private boolean isSuccess(int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }

    /**
     * Minimal headers — some simpler servers reject unfamiliar Sec-* headers.
     * This mirrors the header set that worked in the original implementation.
     */
    private Connection buildMinimalConnection(String url) {
        return Jsoup.connect(url)
                .userAgent(USER_AGENT)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.9")
                .header("Accept-Encoding", "gzip, deflate")
                .timeout(CONNECT_TIMEOUT_MS)
                .maxBodySize(MAX_BODY_BYTES)
                .followRedirects(true)
                .ignoreHttpErrors(true)
                .sslSocketFactory(InsecureSslSocketFactory.create())
                .method(Connection.Method.GET);
    }

    @Override
    public boolean isUrlAccessible(String urlString) {
        try {
            validateUrlScheme(urlString);
            blockSsrf(urlString);

            int status = buildMinimalConnection(urlString)
                    .method(Connection.Method.HEAD)
                    .execute()
                    .statusCode();

            return status >= 200 && status < 400;
        } catch (Exception e) {
            log.warn("URL not accessible: {} - {}", urlString, e.getMessage());
            return false;
        }
    }

    @Override
    public String extractPageTitle(String htmlContent) {
        if (htmlContent == null || htmlContent.isBlank()) return null;
        Document doc = Jsoup.parse(htmlContent);
        String title = doc.title();
        return (title != null && !title.isBlank()) ? title.trim() : null;
    }

    @Override
    public String extractMetaDescription(String htmlContent) {
        if (htmlContent == null || htmlContent.isBlank()) return null;
        return extractMetaDescription(Jsoup.parse(htmlContent));
    }

    // ──────────────────────────────────────────────────────────────────────
    //  SSRF protection  (mirrors Gophish dialer.go deny list)
    // ──────────────────────────────────────────────────────────────────────

    private void validateUrlScheme(String urlString) {
        try {
            URI uri = URI.create(urlString);
            String scheme = uri.getScheme();
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
                throw new ServiceException("Only http and https URLs are allowed", HttpStatus.BAD_REQUEST);
            }
        } catch (IllegalArgumentException e) {
            throw new ServiceException("Invalid URL: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    private void blockSsrf(String urlString) {
        try {
            URL url = new URL(urlString);
            String host = url.getHost();
            InetAddress[] addresses = InetAddress.getAllByName(host);
            for (InetAddress addr : addresses) {
                if (addr.isLoopbackAddress()
                        || addr.isLinkLocalAddress()
                        || addr.isSiteLocalAddress()
                        || addr.isAnyLocalAddress()
                        || addr.isMulticastAddress()
                        || isInDeniedRange(addr.getHostAddress())) {
                    throw new ServiceException(
                            "Upstream connection denied to internal host",
                            HttpStatus.BAD_REQUEST);
                }
            }
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException(
                    "Unable to resolve host: " + e.getMessage(),
                    HttpStatus.BAD_REQUEST);
        }
    }

    private boolean isInDeniedRange(String ip) {
        for (String prefix : DENIED_IP_PREFIXES) {
            if (ip.startsWith(prefix)) return true;
        }
        return ip.equals("255.255.255.255");
    }

    // ──────────────────────────────────────────────────────────────────────
    //  Gophish-equivalent DOM transforms
    // ──────────────────────────────────────────────────────────────────────

    /**
     * Inject {@code <base href="…">} into {@code <head>} when none exists,
     * exactly like Gophish's {@code d.Find("head base").Length() == 0} check.
     */
    private void injectBaseHref(Document doc, String baseUrl) {
        if (doc.head().select("base").isEmpty()) {
            doc.head().prependChild(
                    new Element("base").attr("href", baseUrl));
        }
    }

    /**
     * For each {@code <form>}, store the original action URL in a hidden
     * input named {@code __original_url} — mirrors Gophish exactly.
     */
    private void processFormActions(Document doc, String pageUrl) {
        for (Element form : doc.select("form")) {
            String action = form.attr("action");
            String resolvedAction;
            if (action.isEmpty()) {
                resolvedAction = pageUrl;
            } else if (!action.startsWith("http")) {
                resolvedAction = resolveUrl(action, pageUrl);
            } else {
                resolvedAction = action;
            }
            form.prepend(
                    "<input type=\"hidden\" name=\"__original_url\" value=\""
                    + resolvedAction + "\"/>");
        }
    }

    /**
     * Guarantee a {@code <base href>} exists in the final HTML even after
     * sanitization (the sanitizer strips {@code <base>} by design).
     */
    private String ensureBaseHref(String html, String baseUrl) {
        Document doc = Jsoup.parse(html);
        doc.outputSettings().prettyPrint(false);
        if (doc.head().select("base").isEmpty()) {
            doc.head().prependChild(
                    new Element("base").attr("href", baseUrl));
        }
        return doc.html();
    }

    // ──────────────────────────────────────────────────────────────────────
    //  Metadata / asset extraction  (Jsoup-based, replaces regex)
    // ──────────────────────────────────────────────────────────────────────

    private String extractMetaDescription(Document doc) {
        Element meta = doc.selectFirst("meta[name=description]");
        if (meta != null) {
            String content = meta.attr("content");
            return content.isBlank() ? null : content.trim();
        }
        return null;
    }

    private List<String> extractAssets(Document doc, String baseUrl) {
        Set<String> assets = new HashSet<>();

        for (Element img : doc.select("img[src]")) {
            assets.add(resolveUrl(img.attr("src"), baseUrl));
        }
        for (Element link : doc.select("link[href][rel=stylesheet]")) {
            assets.add(resolveUrl(link.attr("href"), baseUrl));
        }
        for (Element link : doc.select("link[href]")) {
            String href = link.attr("href");
            if (href.endsWith(".css")) {
                assets.add(resolveUrl(href, baseUrl));
            }
        }
        for (Element script : doc.select("script[src]")) {
            assets.add(resolveUrl(script.attr("src"), baseUrl));
        }

        return new ArrayList<>(assets);
    }

    private AssetLocalizationSummary localizeAssets(
            Document doc, String pageUrl, CookieJar cookieJar, int crawlDepth, List<String> warnings) {
        List<String> assetUrls = extractAssets(doc, pageUrl);
        assetUrls.sort(Comparator.naturalOrder());
        int found = Math.min(assetUrls.size(), maxAssetCount);
        int localized = 0;
        int failed = 0;
        int consumedBudget = 0;
        Map<String, String> replaced = new HashMap<>();

        for (int i = 0; i < found; i++) {
            String assetUrl = assetUrls.get(i);
            if (replaced.containsKey(assetUrl)) {
                continue;
            }
            try {
                blockSsrf(assetUrl);
                AssetFetchResult fetched = fetchAssetAsDataUri(assetUrl, pageUrl, cookieJar);
                if (!fetched.success()) {
                    failed++;
                    warnings.add("Asset fetch failed: " + assetUrl + " reason=" + fetched.reason());
                    continue;
                }
                if (consumedBudget + fetched.sizeBytes() > totalAssetByteBudget) {
                    failed++;
                    warnings.add("Asset budget exceeded, skipping: " + assetUrl);
                    continue;
                }
                consumedBudget += fetched.sizeBytes();
                replaced.put(assetUrl, fetched.dataUri());
                localized++;
            } catch (Exception ex) {
                failed++;
                warnings.add("Asset localization exception: " + assetUrl + " reason=" + ex.getMessage());
            }
        }

        rewriteAssetsInDocument(doc, pageUrl, replaced);
        rewriteCssInlineUrls(doc, pageUrl, replaced, crawlDepth, cookieJar, warnings);

        return new AssetLocalizationSummary(found, localized, failed);
    }

    private AssetFetchResult fetchAssetAsDataUri(String assetUrl, String referer, CookieJar cookieJar) {
        try {
            Connection.Response response = Jsoup.connect(assetUrl)
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .maxBodySize(maxAssetBytes)
                    .timeout(CONNECT_TIMEOUT_MS)
                    .sslSocketFactory(InsecureSslSocketFactory.create())
                    .header("User-Agent", USER_AGENT)
                    .header("Accept-Encoding", "gzip, deflate")
                    .header("Referer", referer)
                    .header("Cookie", cookieJar.asHeader())
                    .execute();
            if (!isSuccess(response.statusCode())) {
                return AssetFetchResult.failed("status_" + response.statusCode());
            }
            byte[] bytes = response.bodyAsBytes();
            if (bytes == null || bytes.length == 0) {
                return AssetFetchResult.failed("empty_body");
            }
            if (bytes.length > maxAssetBytes) {
                return AssetFetchResult.failed("asset_too_large");
            }
            String contentType = response.contentType();
            if (contentType == null || contentType.isBlank()) {
                contentType = guessContentType(assetUrl);
            }
            String encoded = Base64.getEncoder().encodeToString(bytes);
            String dataUri = "data:" + contentType + ";base64," + encoded;
            return AssetFetchResult.success(dataUri, bytes.length);
        } catch (Exception e) {
            return AssetFetchResult.failed("fetch_exception");
        }
    }

    private String guessContentType(String url) {
        String lower = url.toLowerCase();
        if (lower.endsWith(".css")) return "text/css";
        if (lower.endsWith(".js")) return "application/javascript";
        if (lower.endsWith(".svg")) return "image/svg+xml";
        if (lower.endsWith(".png")) return MimeTypeUtils.IMAGE_PNG_VALUE;
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return MimeTypeUtils.IMAGE_JPEG_VALUE;
        if (lower.endsWith(".gif")) return MimeTypeUtils.IMAGE_GIF_VALUE;
        if (lower.endsWith(".woff2")) return "font/woff2";
        if (lower.endsWith(".woff")) return "font/woff";
        return MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE;
    }

    private void rewriteAssetsInDocument(Document doc, String pageUrl, Map<String, String> replaced) {
        for (Element img : doc.select("img[src]")) {
            rewriteAttribute(img, "src", pageUrl, replaced);
        }
        for (Element script : doc.select("script[src]")) {
            rewriteAttribute(script, "src", pageUrl, replaced);
        }
        for (Element link : doc.select("link[href]")) {
            rewriteAttribute(link, "href", pageUrl, replaced);
            if (link.hasAttr("integrity") && link.attr("href").startsWith("data:")) {
                link.removeAttr("integrity");
                link.removeAttr("crossorigin");
            }
        }
    }

    private void rewriteAttribute(Element element, String attr, String pageUrl, Map<String, String> replaced) {
        String current = element.attr(attr);
        String resolved = resolveUrl(current, pageUrl);
        String localized = replaced.get(resolved);
        if (localized != null) {
            element.attr(attr, localized);
        }
    }

    private void rewriteCssInlineUrls(
            Document doc, String pageUrl, Map<String, String> replaced, int crawlDepth,
            CookieJar cookieJar, List<String> warnings) {
        // Rewrite style attributes
        for (Element element : doc.select("[style]")) {
            String style = element.attr("style");
            element.attr("style", rewriteCssText(style, pageUrl, replaced, cookieJar, crawlDepth, warnings));
        }

        // Rewrite <style> blocks
        for (Element styleTag : doc.select("style")) {
            styleTag.text(rewriteCssText(styleTag.data(), pageUrl, replaced, cookieJar, crawlDepth, warnings));
        }
    }

    private String rewriteCssText(
            String cssText, String baseUrl, Map<String, String> replaced, CookieJar cookieJar,
            int crawlDepth, List<String> warnings) {
        if (cssText == null || cssText.isBlank()) {
            return cssText;
        }
        String rewritten = cssText;

        Matcher importMatcher = CSS_IMPORT_PATTERN.matcher(rewritten);
        StringBuffer importBuffer = new StringBuffer();
        while (importMatcher.find()) {
            String importPath = importMatcher.group(1);
            String resolved = resolveUrl(importPath, baseUrl);
            String localized = replaced.get(resolved);
            if (localized == null && crawlDepth > 0) {
                AssetFetchResult fetched = fetchAssetAsDataUri(resolved, baseUrl, cookieJar);
                if (fetched.success()) {
                    localized = fetched.dataUri();
                    replaced.put(resolved, localized);
                } else {
                    warnings.add("Failed to localize CSS import: " + resolved);
                }
            }
            if (localized != null) {
                importMatcher.appendReplacement(importBuffer, Matcher.quoteReplacement("@import url('" + localized + "')"));
            } else {
                importMatcher.appendReplacement(importBuffer, Matcher.quoteReplacement(importMatcher.group(0)));
            }
        }
        importMatcher.appendTail(importBuffer);
        rewritten = importBuffer.toString();

        Matcher urlMatcher = CSS_URL_PATTERN.matcher(rewritten);
        StringBuffer sb = new StringBuffer();
        while (urlMatcher.find()) {
            String raw = urlMatcher.group(2).trim();
            String resolved = resolveUrl(raw, baseUrl);
            String localized = replaced.get(resolved);
            if (localized == null && crawlDepth > 0) {
                AssetFetchResult fetched = fetchAssetAsDataUri(resolved, baseUrl, cookieJar);
                if (fetched.success()) {
                    localized = fetched.dataUri();
                    replaced.put(resolved, localized);
                }
            }
            if (localized != null) {
                urlMatcher.appendReplacement(sb, Matcher.quoteReplacement("url('" + localized + "')"));
            } else {
                urlMatcher.appendReplacement(sb, Matcher.quoteReplacement(urlMatcher.group(0)));
            }
        }
        urlMatcher.appendTail(sb);
        return sb.toString();
    }

    // ──────────────────────────────────────────────────────────────────────
    //  URL helpers
    // ──────────────────────────────────────────────────────────────────────

    private String normalizeUrl(String urlString) {
        if (urlString == null) return "";
        urlString = urlString.trim();
        if (!urlString.startsWith("http://") && !urlString.startsWith("https://")) {
            urlString = "https://" + urlString;
        }
        return urlString;
    }

    private String resolveUrl(String src, String baseUrl) {
        if (src == null || src.isBlank()) return baseUrl;
        if (src.startsWith("data:")) return src;
        if (src.startsWith("http://") || src.startsWith("https://")) return src;
        try {
            return new URL(new URL(baseUrl), src).toExternalForm();
        } catch (Exception e) {
            if (src.startsWith("//")) return "https:" + src;
            return baseUrl + (src.startsWith("/") ? "" : "/") + src;
        }
    }

    private record FetchOutcome(
            Document document,
            String finalUrl,
            int statusCode,
            String mode,
            boolean blocked,
            String blockedReason,
            List<String> blockedSignals,
            CookieJar cookieJar
    ) { }

    private record AssetLocalizationSummary(int foundCount, int localizedCount, int failedCount) {
        static AssetLocalizationSummary empty() {
            return new AssetLocalizationSummary(0, 0, 0);
        }
    }

    private record AssetFetchResult(boolean success, String dataUri, int sizeBytes, String reason) {
        static AssetFetchResult success(String dataUri, int sizeBytes) {
            return new AssetFetchResult(true, dataUri, sizeBytes, null);
        }

        static AssetFetchResult failed(String reason) {
            return new AssetFetchResult(false, null, 0, reason);
        }
    }

    // ──────────────────────────────────────────────────────────────────────
    //  Insecure SSL factory  (mirrors Gophish InsecureSkipVerify: true)
    // ──────────────────────────────────────────────────────────────────────

    private static final class InsecureSslSocketFactory {
        private InsecureSslSocketFactory() {}

        static javax.net.ssl.SSLSocketFactory create() {
            try {
                javax.net.ssl.TrustManager[] trustAll = {
                        new javax.net.ssl.X509TrustManager() {
                            @Override
                            public void checkClientTrusted(
                                    java.security.cert.X509Certificate[] chain, String authType) { }
                            @Override
                            public void checkServerTrusted(
                                    java.security.cert.X509Certificate[] chain, String authType) { }
                            @Override
                            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                                return new java.security.cert.X509Certificate[0];
                            }
                        }
                };
                javax.net.ssl.SSLContext ctx = javax.net.ssl.SSLContext.getInstance("TLS");
                ctx.init(null, trustAll, new java.security.SecureRandom());
                return ctx.getSocketFactory();
            } catch (Exception e) {
                throw new RuntimeException("Failed to create insecure SSL factory", e);
            }
        }
    }
}
