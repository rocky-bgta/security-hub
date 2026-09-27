package com.aspire.asat.phishing.ai.adapter;

import com.aspire.asat.phishing.ai.model.AiEmailRequest;
import com.aspire.asat.phishing.ai.model.AiFixHtmlPageRequest;
import com.aspire.asat.phishing.ai.model.GeminiModel;
import com.aspire.asat.phishing.ai.model.AiGeneratedEmailContent;
import com.aspire.asat.phishing.ai.model.AiGeneratedLandingContent;
import com.aspire.asat.phishing.ai.model.AiLandingPageRequest;
import com.aspire.asat.phishing.ai.model.AiResolvedCredentials;
import com.aspire.asat.phishing.ai.prompt.AiProviderPrompts;
import com.aspire.asat.phishing.dto.enums.AiProviderType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeminiAdapter implements AiProviderAdapter {

    private static final AiProviderType PROVIDER = AiProviderType.GEMINI;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${ai.providers.gemini.base-url:https://generativelanguage.googleapis.com}")
    private String geminiBaseUrl;

    @Override
    public AiProviderType getProviderType() {
        return PROVIDER;
    }

    @Override
    public AiGeneratedEmailContent generateEmailTemplate(AiEmailRequest request) {
        try {
            validateRequest(request);
            String model = resolveModel(request.getModel());
            String endpoint = resolveGenerateContentEndpoint(model);
            String prompt = AiProviderPrompts.emailGenerationPrompt(request);

            JsonNode json = callGenerateContent(endpoint, request.getApiKey(), prompt, emailResponseSchema())
                    .block(Duration.ofSeconds(180));

            String content = extractTextFromGenerateContent(json);
            if (content == null) {
                return AiGeneratedEmailContent.builder()
                        .success(false)
                        .errorMessage("Gemini returned empty response")
                        .build();
            }

            JsonNode parsed = parseJsonObject(content);
            if (parsed == null) {
                return AiGeneratedEmailContent.builder()
                        .success(false)
                        .errorMessage("Gemini returned non-JSON output")
                        .build();
            }

            return AiGeneratedEmailContent.builder()
                    .htmlBody(parsed.path("htmlBody").asText(null))
                    .textBody(parsed.path("textBody").asText(null))
                    .suggestedDifficulty(parsed.path("suggestedDifficulty").asText(null))
                    .success(true)
                    .errorMessage(null)
                    .build();
        } catch (Exception e) {
            log.error("Gemini email generation API call failed: {}", formatGeminiFailure(e), e);
            return AiGeneratedEmailContent.builder()
                    .success(false)
                    .errorMessage("Gemini email generation failed: " + formatGeminiFailure(e))
                    .build();
        }
    }

    @Override
    public AiGeneratedLandingContent generateLandingPage(AiLandingPageRequest request) {
        try {
            validateRequest(request);
            String model = resolveModel(request.getModel());
            String endpoint = resolveGenerateContentEndpoint(model);
            String prompt = AiProviderPrompts.landingPageGenerationPrompt(request);

            JsonNode json = callGenerateContent(endpoint, request.getApiKey(), prompt, landingPageResponseSchema())
                    .block(Duration.ofSeconds(180));

            String content = extractTextFromGenerateContent(json);
            if (content == null) {
                return AiGeneratedLandingContent.builder()
                        .success(false)
                        .errorMessage("Gemini returned empty response")
                        .build();
            }

            JsonNode parsed = parseJsonObject(content);
            if (parsed == null) {
                return AiGeneratedLandingContent.builder()
                        .success(false)
                        .errorMessage("Gemini returned non-JSON output")
                        .build();
            }

            return AiGeneratedLandingContent.builder()
                    .htmlContent(parsed.path("htmlContent").asText(null))
                    .title(parsed.path("title").asText(null))
                    .suggestedDifficulty(parsed.path("suggestedDifficulty").asText(null))
                    .success(true)
                    .errorMessage(null)
                    .build();
        } catch (Exception e) {
            log.error("Gemini landing page generation API call failed: {}", e);
            return AiGeneratedLandingContent.builder()
                    .success(false)
                    .errorMessage("Gemini landing page generation failed: " + formatGeminiFailure(e))
                    .build();
        }
    }

    @Override
    public String fixHtmlPage(AiFixHtmlPageRequest request, AiResolvedCredentials credentials) {
        try {
            validateRequest(request);
            validateCredentials(credentials);
            String model = resolveModel(request.getModel());
            String endpoint = resolveGenerateContentEndpoint(model);
            String prompt = AiProviderPrompts.fixHtmlPagePrompt(request);

            JsonNode json = callGenerateContent(endpoint, credentials.getApiKey(), prompt, null)
                    .block(Duration.ofSeconds(180));

            String content = extractTextFromGenerateContent(json);
            if (content == null || content.isBlank()) {
                throw new IllegalStateException("Gemini returned empty response");
            }
            return content;
        } catch (Exception e) {
            log.error("Gemini html-page fix API call failed: {}", e);
            throw new IllegalStateException("Gemini html-page fix failed: " + formatGeminiFailure(e), e);
        }
    }

    @Override
    public String fixEmailTemplate(AiFixHtmlPageRequest request, AiResolvedCredentials credentials) {
        try {
            validateRequest(request);
            validateCredentials(credentials);
            String model = resolveModel(request.getModel());
            String endpoint = resolveGenerateContentEndpoint(model);
            String prompt = AiProviderPrompts.fixEmailTemplatePrompt(request);

            JsonNode json = callGenerateContent(endpoint, credentials.getApiKey(), prompt, null)
                    .block(Duration.ofSeconds(180));

            String content = extractTextFromGenerateContent(json);
            if (content == null || content.isBlank()) {
                throw new IllegalStateException("Gemini returned empty response");
            }
            return content;
        } catch (Exception e) {
            throw new IllegalStateException("Gemini email-template fix failed: " + formatGeminiFailure(e), e);
        }
    }

    private void validateRequest(AiEmailRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("AiEmailRequest is required");
        }
        if (!PROVIDER.equals(request.getProviderType())) {
            throw new IllegalArgumentException("Invalid providerType for GeminiAdapter: " + request.getProviderType());
        }
        if (request.getApiKey() == null || request.getApiKey().isBlank()) {
            throw new IllegalArgumentException("apiKey is required for Gemini");
        }
    }

    private void validateRequest(AiLandingPageRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("AiLandingPageRequest is required");
        }
        if (!PROVIDER.equals(request.getProviderType())) {
            throw new IllegalArgumentException("Invalid providerType for GeminiAdapter: " + request.getProviderType());
        }
        if (request.getApiKey() == null || request.getApiKey().isBlank()) {
            throw new IllegalArgumentException("apiKey is required for Gemini");
        }
    }

    private void validateRequest(AiFixHtmlPageRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("AiFixHtmlPageRequest is required");
        }
        if (!PROVIDER.equals(request.getProviderType())) {
            throw new IllegalArgumentException("Invalid providerType for GeminiAdapter: " + request.getProviderType());
        }
        if (request.getInput() == null) {
            throw new IllegalArgumentException("input is required");
        }
    }

    private static void validateCredentials(AiResolvedCredentials credentials) {
        if (credentials == null || credentials.getApiKey() == null || credentials.getApiKey().isBlank()) {
            throw new IllegalArgumentException("apiKey is required for Gemini");
        }
    }

    private String resolveModel(String model) {
        if (model == null || model.isBlank()) {
            return GeminiModel.defaultModel();
        }
        String trimmed = model.trim();
        if (!GeminiModel.isAllowed(trimmed)) {
            throw new IllegalArgumentException("Model not allowed for Gemini");
        }
        return trimmed;
    }

    private String resolveGenerateContentEndpoint(String model) {
        String base = (geminiBaseUrl == null || geminiBaseUrl.isBlank())
                ? "https://generativelanguage.googleapis.com"
                : geminiBaseUrl.trim().replaceAll("/$", "");
        if (base.contains(":generateContent")) {
            return base;
        }
        if (base.contains("/v1beta/models/")) {
            return base + ":generateContent";
        }
        return base + "/v1beta/models/" + model + ":generateContent";
    }

    private Mono<JsonNode> callGenerateContent(String endpoint, String apiKey, String prompt, Map<String, Object> responseSchema) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("contents", List.of(
                Map.of(
                        "role", "user",
                        "parts", List.of(Map.of("text", prompt))
                )
        ));
        Map<String, Object> generationConfig = new LinkedHashMap<>();
        generationConfig.put("temperature", 0.5);
        generationConfig.put("maxOutputTokens", 2000);
        if (responseSchema != null) {
            generationConfig.put("responseMimeType", "application/json");
            generationConfig.put("responseSchema", responseSchema);
        } else {
            generationConfig.put("responseMimeType", "text/plain");
        }
        payload.put("generationConfig", generationConfig);
        payload.put("safetySettings", List.of(
                Map.of(
                        "category", "HARM_CATEGORY_HARASSMENT",
                        "threshold", "BLOCK_NONE"
                )
        ));

        return webClient.post()
                .uri(endpoint + "?key=" + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(payload)
                .retrieve()
                .onStatus(status -> status.value() == 401 || status.value() == 403, resp ->
                        resp.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(body -> Mono.error(new IllegalArgumentException(
                                        "Gemini authentication failed: " + truncate(body, 1000)))))
                .onStatus(status -> status.value() == 429, resp ->
                        resp.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(body -> Mono.error(new IllegalArgumentException(
                                        "Gemini rate limited: " + truncate(body, 1000)))))
                .onStatus(status -> status.is5xxServerError(), resp ->
                        resp.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(body -> Mono.error(new IllegalStateException(
                                        "Gemini HTTP 5xx " + resp.statusCode() + ": " + truncate(body, 1000)))))
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofSeconds(180))
                .retryWhen(Retry.backoff(2, Duration.ofSeconds(2))
                        .filter(ex -> ex instanceof IllegalStateException
                                && ex.getMessage() != null
                                && ex.getMessage().startsWith("Gemini HTTP 5xx"))
                        .onRetryExhaustedThrow((spec, signal) -> signal.failure()));
    }

    private String extractTextFromGenerateContent(JsonNode root) {
        if (root == null || root.isNull()) {
            return null;
        }
        // Some Gemini responses expose flattened text directly.
        String directText = root.path("text").asText(null);
        if (directText != null && !directText.isBlank()) {
            return directText;
        }

        JsonNode candidate = root.path("candidates").isArray() && root.path("candidates").size() > 0
                ? root.path("candidates").get(0)
                : null;
        if (candidate == null || candidate.isNull()) {
            return null;
        }

        String signature = candidate.path("content").path("thoughtSignature").asText(null);
        if (signature != null && !signature.isBlank()) {
            log.debug("Gemini thoughtSignature captured");
        }

        JsonNode parts = candidate.path("content").path("parts");
        if (!parts.isArray()) {
            return null;
        }
        StringBuilder combined = new StringBuilder();
        for (JsonNode part : parts) {
            String text = part.path("text").asText(null);
            if (text != null && !text.isBlank()) {
                if (combined.length() > 0) {
                    combined.append('\n');
                }
                combined.append(text);
            }
        }
        return combined.length() > 0 ? combined.toString() : null;
    }

    private JsonNode parseJsonObject(String content) {
        if (content == null || content.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readTree(content);
        } catch (Exception ignored) {
            try {
                String withoutCodeFence = stripMarkdownCodeFence(content);
                if (!withoutCodeFence.equals(content)) {
                    return objectMapper.readTree(withoutCodeFence);
                }
            } catch (Exception ignored2) {
                // Continue to structural extraction fallback.
            }
            try {
                String candidate = extractFirstBalancedJsonObject(content);
                if (candidate != null) {
                    return objectMapper.readTree(candidate);
                }
            } catch (Exception ignored3) {
                return null;
            }
        }
        return null;
    }

    private static String stripMarkdownCodeFence(String content) {
        String trimmed = content.trim();
        if (!trimmed.startsWith("```")) {
            return content;
        }
        int firstNewline = trimmed.indexOf('\n');
        if (firstNewline < 0) {
            return content;
        }
        int closingFence = trimmed.lastIndexOf("```");
        if (closingFence <= firstNewline) {
            return content;
        }
        return trimmed.substring(firstNewline + 1, closingFence).trim();
    }

    private static String extractFirstBalancedJsonObject(String content) {
        boolean inString = false;
        boolean escaped = false;
        int depth = 0;
        int start = -1;

        for (int i = 0; i < content.length(); i++) {
            char ch = content.charAt(i);

            if (inString) {
                if (escaped) {
                    escaped = false;
                } else if (ch == '\\') {
                    escaped = true;
                } else if (ch == '"') {
                    inString = false;
                }
                continue;
            }

            if (ch == '"') {
                inString = true;
                continue;
            }

            if (ch == '{') {
                if (depth == 0) {
                    start = i;
                }
                depth++;
                continue;
            }

            if (ch == '}' && depth > 0) {
                depth--;
                if (depth == 0 && start >= 0) {
                    return content.substring(start, i + 1);
                }
            }
        }
        return null;
    }

    private static String formatGeminiFailure(Throwable e) {
        Throwable t = Exceptions.unwrap(e);
        if (t instanceof WebClientResponseException w) {
            String body = truncate(w.getResponseBodyAsString(), 600);
            return w.getStatusCode().value() + " " + w.getStatusText()
                    + (body.isBlank() ? "" : (" - " + body));
        }
        return t.getMessage() != null ? t.getMessage() : t.getClass().getSimpleName();
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        String t = s.trim();
        return t.length() <= max ? t : t.substring(0, max) + "...";
    }

    private static Map<String, Object> emailResponseSchema() {
        return Map.of(
                "type", "OBJECT",
                "properties", Map.of(
                        "htmlBody", Map.of("type", "STRING"),
                        "textBody", Map.of("type", "STRING"),
                        "emailSubject", Map.of("type", "STRING"),
                        "suggestedDifficulty", Map.of("type", "STRING")
                ),
                "required", List.of("htmlBody", "textBody", "emailSubject", "suggestedDifficulty")
        );
    }

    private static Map<String, Object> landingPageResponseSchema() {
        return Map.of(
                "type", "OBJECT",
                "properties", Map.of(
                        "htmlContent", Map.of("type", "STRING"),
                        "title", Map.of("type", "STRING"),
                        "suggestedDifficulty", Map.of("type", "STRING")
                ),
                "required", List.of("htmlContent", "title", "suggestedDifficulty")
        );
    }

}
