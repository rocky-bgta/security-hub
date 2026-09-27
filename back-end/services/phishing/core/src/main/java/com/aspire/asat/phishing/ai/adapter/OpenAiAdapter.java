package com.aspire.asat.phishing.ai.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.aspire.asat.phishing.ai.model.AiEmailRequest;
import com.aspire.asat.phishing.ai.model.AiFixHtmlPageRequest;
import com.aspire.asat.phishing.ai.model.AiGeneratedEmailContent;
import com.aspire.asat.phishing.ai.model.AiGeneratedLandingContent;
import com.aspire.asat.phishing.ai.model.AiLandingPageRequest;
import com.aspire.asat.phishing.ai.model.AiResolvedCredentials;
import com.aspire.asat.phishing.ai.model.OpenAiModel;
import com.aspire.asat.phishing.ai.prompt.AiProviderPrompts;
import com.aspire.asat.phishing.dto.enums.AiProviderType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * OpenAI adapter (first provider implementation).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OpenAiAdapter implements AiProviderAdapter {

    private static final AiProviderType PROVIDER = AiProviderType.OPENAI;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${ai.providers.openai.base-url:https://api.openai.com}")
    private String openAiBaseUrl;

    @Override
    public AiProviderType getProviderType() {
        return PROVIDER;
    }

    @Override
    public AiGeneratedEmailContent generateEmailTemplate(AiEmailRequest request) {
        try {
            validateRequest(request);

            String model = resolveModel(request.getModel());
            String endpoint = resolveResponsesEndpoint();

            String prompt = AiProviderPrompts.emailGenerationPrompt(request);
            JsonNode json = callResponses(
                    endpoint,
                    request.getApiKey(),
                    model,
                    prompt
            ).block(Duration.ofSeconds(180));

            String content = extractOutputTextFromResponses(json);
            if (content == null) {
                return AiGeneratedEmailContent.builder()
                        .success(false)
                        .errorMessage("OpenAI returned empty response")
                        .build();
            }

            JsonNode parsed = parseJsonObject(content);
            if (parsed == null) {
                return AiGeneratedEmailContent.builder()
                        .success(false)
                        .errorMessage("OpenAI returned non-JSON output")
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
            log.error("OpenAI email generation API call failed: {}", e);
            return AiGeneratedEmailContent.builder()
                    .success(false)
                    .errorMessage("OpenAI email generation failed: " + formatOpenAiFailure(e))
                    .build();
        }
    }

    @Override
    public AiGeneratedLandingContent generateLandingPage(AiLandingPageRequest request) {
        try {
            validateRequest(request);

            String model = resolveModel(request.getModel());
            String endpoint = resolveResponsesEndpoint();

            String prompt = AiProviderPrompts.landingPageGenerationPrompt(request);
            JsonNode json = callResponses(
                    endpoint,
                    request.getApiKey(),
                    model,
                    prompt
            ).block(Duration.ofSeconds(180));

            String content = extractOutputTextFromResponses(json);
            if (content == null) {
                return AiGeneratedLandingContent.builder()
                        .success(false)
                        .errorMessage("OpenAI returned empty response")
                        .build();
            }

            JsonNode parsed = parseJsonObject(content);
            if (parsed == null) {
                return AiGeneratedLandingContent.builder()
                        .success(false)
                        .errorMessage("OpenAI returned non-JSON output")
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
            log.error("OpenAI landing page generation API call failed: {}", e);
            return AiGeneratedLandingContent.builder()
                    .success(false)
                    .errorMessage("OpenAI landing page generation failed: " + formatOpenAiFailure(e))
                    .build();
        }
    }

    @Override
    public String fixHtmlPage(AiFixHtmlPageRequest request, AiResolvedCredentials credentials) {
        try {
            validateRequest(request);
            validateCredentials(credentials);

            String model = resolveModel(request.getModel());
            String endpoint = resolveResponsesEndpoint();
            String prompt = AiProviderPrompts.fixHtmlPagePrompt(request);

            JsonNode json = callGenerateContent(
                    endpoint,
                    credentials.getApiKey(),
                    model,
                    prompt
            ).block(Duration.ofSeconds(120));

            String content = extractOutputTextFromResponses(json);
            if (content == null || content.isBlank()) {
                throw new IllegalStateException("OpenAI returned empty response");
            }
            return content;
        } catch (Exception e) {
            log.error("OpenAI html-page fix API call failed: {}", e);
            throw new IllegalStateException("OpenAI html-page fix failed: " + formatOpenAiFailure(e), e);
        }
    }

    @Override
    public String fixEmailTemplate(AiFixHtmlPageRequest request, AiResolvedCredentials credentials) {
        try {
            validateRequest(request);
            validateCredentials(credentials);

            String model = resolveModel(request.getModel());
            String endpoint = resolveResponsesEndpoint();
            String prompt = AiProviderPrompts.fixEmailTemplatePrompt(request);

            JsonNode json = callGenerateContent(
                    endpoint,
                    credentials.getApiKey(),
                    model,
                    prompt
            ).block(Duration.ofSeconds(180));

            String content = extractOutputTextFromResponses(json);
            if (content == null || content.isBlank()) {
                throw new IllegalStateException("OpenAI returned empty response");
            }
            return content;
        } catch (Exception e) {
            throw new IllegalStateException("OpenAI email-template fix failed: " + formatOpenAiFailure(e), e);
        }
    }

    private void validateRequest(AiEmailRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("AiEmailRequest is required");
        }
        if (!PROVIDER.equals(request.getProviderType())) {
            throw new IllegalArgumentException("Invalid providerType for OpenAiAdapter: " + request.getProviderType());
        }
        if (request.getApiKey() == null || request.getApiKey().isBlank()) {
            throw new IllegalArgumentException("apiKey is required for OpenAI");
        }
    }

    private void validateRequest(AiLandingPageRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("AiLandingPageRequest is required");
        }
        if (!PROVIDER.equals(request.getProviderType())) {
            throw new IllegalArgumentException("Invalid providerType for OpenAiAdapter: " + request.getProviderType());
        }
        if (request.getApiKey() == null || request.getApiKey().isBlank()) {
            throw new IllegalArgumentException("apiKey is required for OpenAI");
        }
    }

    private void validateRequest(AiFixHtmlPageRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("AiFixHtmlPageRequest is required");
        }
        if (!PROVIDER.equals(request.getProviderType())) {
            throw new IllegalArgumentException("Invalid providerType for OpenAiAdapter: " + request.getProviderType());
        }
        if (request.getInput() == null) {
            throw new IllegalArgumentException("input is required");
        }
    }

    private static void validateCredentials(AiResolvedCredentials credentials) {
        if (credentials == null || credentials.getApiKey() == null || credentials.getApiKey().isBlank()) {
            throw new IllegalArgumentException("apiKey is required for OpenAI");
        }
    }

    private String resolveModel(String model) {
        if (model == null || model.isBlank()) {
            return OpenAiModel.defaultModel();
        }
        String trimmed = model.trim();
        if (!OpenAiModel.isAllowed(trimmed)) {
            throw new IllegalArgumentException("Model not allowed for OpenAI");
        }
        return trimmed;
    }

    private String resolveBaseUrl() {
        if (openAiBaseUrl == null || openAiBaseUrl.isBlank()) {
            return "https://api.openai.com";
        }
        return openAiBaseUrl.trim().replaceAll("/$", "");
    }

    /**
     * OpenAI Responses API: {@code POST https://api.openai.com/v1/responses}.
     * Accepts either API origin ({@code https://api.openai.com}) or the full responses URL.
     */
    private String resolveResponsesEndpoint() {
        String configuredBase = resolveBaseUrl();
        if (configuredBase.contains("/v1/responses")) {
            return configuredBase.replaceAll("/$", "");
        }
        return configuredBase + "/v1/responses";
    }

    private JsonNode parseJsonObject(String content) {
        try {
            return objectMapper.readTree(content);
        } catch (Exception ignored) {
            // Fallback: try to extract the first {...} block.
            try {
                int start = content.indexOf('{');
                int end = content.lastIndexOf('}');
                if (start >= 0 && end > start) {
                    String candidate = content.substring(start, end + 1);
                    return objectMapper.readTree(candidate);
                }
            } catch (Exception ignored2) {
                return null;
            }
        }
        return null;
    }

    /**
     * Calls OpenAI <a href="https://api.openai.com/v1/responses">Responses API</a> (not Chat Completions).
     */
    private Mono<JsonNode> callResponses(
            String endpoint,
            String apiKey,
            String model,
            String prompt) {

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", model);
        payload.put(
                "instructions",
                "You generate phishing simulation content for authorized security awareness training only. "
                        + "Follow the user prompt exactly. When JSON is requested, respond with valid JSON only."
        );
        payload.put("input", prompt);
        payload.put("temperature", 0.2);
        payload.put("max_output_tokens", 10000);

        return webClient.post()
                .uri(endpoint)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .bodyValue(payload)
                .retrieve()
                .onStatus(status -> status.value() == 401 || status.value() == 403, resp ->
                        resp.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(body -> Mono.error(new IllegalArgumentException(
                                        "OpenAI authentication failed: " + truncate(body, 1000)))))
                // Do not retry rate limits — retrying makes 429 worse.
                .onStatus(status -> status.value() == 429, resp ->
                        resp.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(body -> Mono.error(new IllegalArgumentException(
                                        "OpenAI rate limited: " + truncate(body, 1000)))))
                // Only 5xx is treated as transient and retried below.
                .onStatus(status -> status.is5xxServerError(), resp ->
                        resp.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(body -> Mono.error(new IllegalStateException(
                                        "OpenAI HTTP 5xx " + resp.statusCode() + ": " + truncate(body, 600)))))
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofSeconds(180))
                .retryWhen(Retry.backoff(2, Duration.ofSeconds(2))
                        .filter(ex -> ex instanceof IllegalStateException
                                && ex.getMessage() != null
                                && ex.getMessage().startsWith("OpenAI HTTP 5xx"))
                        .onRetryExhaustedThrow((spec, signal) -> signal.failure()));
    }

    private Mono<JsonNode> callGenerateContent(
            String endpoint,
            String apiKey,
            String model,
            String prompt) {
        return callResponses(endpoint, apiKey, model, prompt);
    }

    /**
     * Surfaces root cause instead of only {@code RetryExhaustedException}, and adds HTTP body hints for WebClient errors.
     */
    private static String formatOpenAiFailure(Throwable e) {
        Throwable t = Exceptions.unwrap(e);
        if (t instanceof WebClientResponseException w) {
            String body = truncate(w.getResponseBodyAsString(), 1000);
            return w.getStatusCode().value() + " " + w.getStatusText()
                    + (body.isBlank() ? "" : (" — " + body));
        }
        return t.getMessage() != null ? t.getMessage() : t.getClass().getSimpleName();
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        String t = s.trim();
        return t.length() <= max ? t : t.substring(0, max) + "…";
    }

    /**
     * Parses assistant text from a Responses API JSON body ({@code output[]} with {@code output_text} items).
     */
    private String extractOutputTextFromResponses(JsonNode root) {
        if (root == null || root.isNull()) {
            return null;
        }
        if (root.hasNonNull("error")) {
            JsonNode err = root.get("error");
            String msg = err.path("message").asText(err.toString());
            log.warn("OpenAI response error object: {}", msg);
            return null;
        }
        String status = root.path("status").asText("");
        if (!status.isEmpty() && !"completed".equalsIgnoreCase(status)) {
            log.warn("OpenAI response status not completed: {}", status);
        }
        JsonNode output = root.path("output");
        if (!output.isArray()) {
            return null;
        }
        for (JsonNode item : output) {
            if (!"message".equals(item.path("type").asText())) {
                continue;
            }
            JsonNode content = item.path("content");
            if (!content.isArray()) {
                continue;
            }
            for (JsonNode part : content) {
                if ("output_text".equals(part.path("type").asText())) {
                    String text = part.path("text").asText(null);
                    if (text != null && !text.isBlank()) {
                        return text;
                    }
                }
            }
        }
        return null;
    }
}

