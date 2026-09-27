package com.aspire.asat.phishing.ai.adapter;

import com.aspire.asat.phishing.ai.model.AiEmailRequest;
import com.aspire.asat.phishing.ai.model.AiFixHtmlPageRequest;
import com.aspire.asat.phishing.ai.model.AiGeneratedEmailContent;
import com.aspire.asat.phishing.ai.model.AiGeneratedLandingContent;
import com.aspire.asat.phishing.ai.model.AiLandingPageRequest;
import com.aspire.asat.phishing.ai.model.AiResolvedCredentials;
import com.aspire.asat.phishing.ai.model.ZAiModel;
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
public class ZAiAdapter implements AiProviderAdapter {

    private static final AiProviderType PROVIDER = AiProviderType.ZAI;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${ai.providers.zai.base-url:https://api.z.ai/api/paas/v4}")
    private String zAiBaseUrl;

    @Override
    public AiProviderType getProviderType() {
        return PROVIDER;
    }

    @Override
    public AiGeneratedEmailContent generateEmailTemplate(AiEmailRequest request) {
        try {
            validateRequest(request);

            String model = resolveModel(request.getModel());
            String endpoint = resolveChatCompletionsEndpoint();
            String prompt = AiProviderPrompts.emailGenerationPrompt(request);

            JsonNode json = callChatCompletions(endpoint, request.getApiKey(), model, prompt)
                    .block(Duration.ofSeconds(180));

            String content = extractAssistantContent(json);
            if (content == null || content.isBlank()) {
                return AiGeneratedEmailContent.builder()
                        .success(false)
                        .errorMessage("ZAI returned empty response")
                        .build();
            }

            JsonNode parsed = parseJsonObject(content);
            if (parsed == null) {
                return AiGeneratedEmailContent.builder()
                        .success(false)
                        .errorMessage("ZAI returned non-JSON output")
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
            log.error("ZAI email template generation API call failed: {}", e);
            return AiGeneratedEmailContent.builder()
                    .success(false)
                    .errorMessage("ZAI email generation failed: " + formatZAiFailure(e))
                    .build();
        }
    }

    @Override
    public AiGeneratedLandingContent generateLandingPage(AiLandingPageRequest request) {
        try {
            validateRequest(request);

            String model = resolveModel(request.getModel());
            String endpoint = resolveChatCompletionsEndpoint();
            String prompt = AiProviderPrompts.landingPageGenerationPrompt(request);

            JsonNode json = callChatCompletions(endpoint, request.getApiKey(), model, prompt)
                    .block(Duration.ofSeconds(180));

            String content = extractAssistantContent(json);
            if (content == null || content.isBlank()) {
                return AiGeneratedLandingContent.builder()
                        .success(false)
                        .errorMessage("ZAI returned empty response")
                        .build();
            }

            JsonNode parsed = parseJsonObject(content);
            if (parsed == null) {
                return AiGeneratedLandingContent.builder()
                        .success(false)
                        .errorMessage("ZAI returned non-JSON output")
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
            log.error("ZAI landing page generation API call failed: {}", e);
            return AiGeneratedLandingContent.builder()
                    .success(false)
                    .errorMessage("ZAI landing page generation failed: " + formatZAiFailure(e))
                    .build();
        }
    }

    @Override
    public String fixHtmlPage(AiFixHtmlPageRequest request, AiResolvedCredentials credentials) {
        try {
            validateRequest(request);
            validateCredentials(credentials);

            String model = resolveModel(request.getModel());
            String endpoint = resolveChatCompletionsEndpoint();
            String prompt = AiProviderPrompts.fixHtmlPagePrompt(request);

            JsonNode json = callChatCompletions(endpoint, credentials.getApiKey(), model, prompt)
                    .block(Duration.ofSeconds(120));

            String content = extractAssistantContent(json);
            if (content == null || content.isBlank()) {
                throw new IllegalStateException("ZAI returned empty response");
            }
            return content;
        } catch (Exception e) {
            log.error("ZAI html page fix API call failed: {}", e);
            throw new IllegalStateException("ZAI html-page fix failed: " + formatZAiFailure(e), e);
        }
    }

    @Override
    public String fixEmailTemplate(AiFixHtmlPageRequest request, AiResolvedCredentials credentials) {
        try {
            validateRequest(request);
            validateCredentials(credentials);

            String model = resolveModel(request.getModel());
            String endpoint = resolveChatCompletionsEndpoint();
            String prompt = AiProviderPrompts.fixEmailTemplatePrompt(request);

            JsonNode json = callChatCompletions(endpoint, credentials.getApiKey(), model, prompt)
                    .block(Duration.ofSeconds(180));

            String content = extractAssistantContent(json);
            if (content == null || content.isBlank()) {
                throw new IllegalStateException("ZAI returned empty response");
            }
            return content;
        } catch (Exception e) {
            log.error("ZAI email template fix API call failed: {}", e);
            throw new IllegalStateException("ZAI email-template fix failed: " + formatZAiFailure(e), e);
        }
    }

    private void validateRequest(AiEmailRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("AiEmailRequest is required");
        }
        if (!PROVIDER.equals(request.getProviderType())) {
            throw new IllegalArgumentException("Invalid providerType for ZAiAdapter: " + request.getProviderType());
        }
        if (request.getApiKey() == null || request.getApiKey().isBlank()) {
            throw new IllegalArgumentException("apiKey is required for ZAI");
        }
    }

    private void validateRequest(AiLandingPageRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("AiLandingPageRequest is required");
        }
        if (!PROVIDER.equals(request.getProviderType())) {
            throw new IllegalArgumentException("Invalid providerType for ZAiAdapter: " + request.getProviderType());
        }
        if (request.getApiKey() == null || request.getApiKey().isBlank()) {
            throw new IllegalArgumentException("apiKey is required for ZAI");
        }
    }

    private void validateRequest(AiFixHtmlPageRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("AiFixHtmlPageRequest is required");
        }
        if (!PROVIDER.equals(request.getProviderType())) {
            throw new IllegalArgumentException("Invalid providerType for ZAiAdapter: " + request.getProviderType());
        }
        if (request.getInput() == null) {
            throw new IllegalArgumentException("input is required");
        }
    }

    private static void validateCredentials(AiResolvedCredentials credentials) {
        if (credentials == null || credentials.getApiKey() == null || credentials.getApiKey().isBlank()) {
            throw new IllegalArgumentException("apiKey is required for ZAI");
        }
    }

    private String resolveModel(String model) {
        if (model == null || model.isBlank()) {
            return ZAiModel.defaultModel();
        }
        String trimmed = model.trim();
        if (!ZAiModel.isAllowed(trimmed)) {
            throw new IllegalArgumentException("Model not allowed for ZAI");
        }
        return trimmed;
    }

    private String resolveBaseUrl() {
        if (zAiBaseUrl == null || zAiBaseUrl.isBlank()) {
            return "https://api.z.ai/api/paas/v4";
        }
        return zAiBaseUrl.trim().replaceAll("/$", "");
    }

    private String resolveChatCompletionsEndpoint() {
        String configuredBase = resolveBaseUrl();
        if (configuredBase.endsWith("/chat/completions")) {
            return configuredBase;
        }
        return configuredBase + "/chat/completions";
    }

    private Mono<JsonNode> callChatCompletions(
            String endpoint,
            String apiKey,
            String model,
            String prompt) {

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("model", model);
        payload.put("messages", List.of(
                Map.of(
                        "role", "system",
                        "content", "You are a professional programming assistant"
                ),
                Map.of(
                        "role", "user",
                        "content", prompt
                )
        ));

        return webClient.post()
                .uri(endpoint)
                .header("Authorization", "Bearer " + apiKey)
                .header("Accept-Language", "en-US,en")
                .header("Content-Type", "application/json")
                .bodyValue(payload)
                .retrieve()
                .onStatus(status -> status.value() == 401 || status.value() == 403, resp ->
                        Mono.error(new IllegalArgumentException("ZAI authentication failed")))
                .onStatus(status -> status.value() == 429, resp ->
                        resp.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(body -> Mono.error(new IllegalArgumentException(
                                        "ZAI rate limited: " + truncate(body, 400)))))
                .onStatus(status -> status.is5xxServerError(), resp ->
                        resp.bodyToMono(String.class)
                                .defaultIfEmpty("")
                                .flatMap(body -> Mono.error(new IllegalStateException(
                                        "ZAI HTTP 5xx " + resp.statusCode() + ": " + truncate(body, 600)))))
                .bodyToMono(JsonNode.class)
                .timeout(Duration.ofSeconds(180))
                .retryWhen(Retry.backoff(2, Duration.ofSeconds(2))
                        .filter(ex -> ex instanceof IllegalStateException
                                && ex.getMessage() != null
                                && ex.getMessage().startsWith("ZAI HTTP 5xx"))
                        .onRetryExhaustedThrow((spec, signal) -> signal.failure()));
    }

    private String extractAssistantContent(JsonNode root) {
        if (root == null || root.isNull()) {
            return null;
        }
        JsonNode choices = root.path("choices");
        if (!choices.isArray() || choices.isEmpty()) {
            return null;
        }

        JsonNode message = choices.get(0).path("message");
        JsonNode contentNode = message.path("content");
        if (contentNode.isTextual()) {
            return contentNode.asText(null);
        }
        if (contentNode.isArray()) {
            StringBuilder combined = new StringBuilder();
            for (JsonNode part : contentNode) {
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
        return null;
    }

    private JsonNode parseJsonObject(String content) {
        try {
            return objectMapper.readTree(content);
        } catch (Exception ignored) {
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

    private static String formatZAiFailure(Throwable e) {
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
}
