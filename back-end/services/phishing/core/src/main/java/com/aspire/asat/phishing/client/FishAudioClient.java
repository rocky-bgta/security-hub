package com.aspire.asat.phishing.client;

import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.media.model.ResolvedProviderCredentials;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.File;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Thin client for the Fish Audio voice cloning + text-to-speech APIs.
 *
 * <ul>
 *   <li>{@code POST /model} - create a persistent voice clone model</li>
 *   <li>{@code POST /v1/tts} - synthesize speech from text + reference_id</li>
 *   <li>{@code DELETE /model/{id}} - delete a voice clone model</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FishAudioClient {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String MODEL_HEADER = "model";

    private final WebClient webClient;

    @Value("${fish.base-url:https://api.fish.audio}")
    private String baseUrl;

    @Value("${fish.model:s2-pro}")
    private String ttsModel;

    @Value("${fish.timeout-seconds:120}")
    private long timeoutSeconds;

    @Value("${fish.clone.train-mode:fast}")
    private String cloneTrainMode;

    @Value("${fish.clone.visibility:private}")
    private String cloneVisibility;

    public String createModel(File sampleFile, String title, ResolvedProviderCredentials credentials) {
        String apiKey = requireApiKey(credentials);

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("type", "tts");
        builder.part("title", title == null || title.isBlank() ? "deepfake-voice" : title);
        builder.part("description", "Deepfake wizard voice clone");
        builder.part("visibility", cloneVisibility);
        builder.part("train_mode", cloneTrainMode);
        builder.part("voices", new FileSystemResource(sampleFile));

        try {
            JsonNode response = webClient.post()
                    .uri(resolveBaseUrl(credentials) + "/model")
                    .header(AUTHORIZATION_HEADER, bearerToken(apiKey))
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .block();

            String modelId = extractModelId(response);
            waitUntilTrained(modelId, credentials);
            return modelId;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Fish Audio voice clone failed: " + e.getMessage(),
                    HttpStatus.BAD_GATEWAY, e);
        }
    }

    public byte[] textToSpeech(String referenceId, String text, ResolvedProviderCredentials credentials) {
        String apiKey = requireApiKey(credentials);
        if (referenceId == null || referenceId.isBlank()) {
            throw new ServiceException("referenceId is required for Fish Audio synthesis", HttpStatus.BAD_REQUEST);
        }
        if (text == null || text.isBlank()) {
            throw new ServiceException("text is required for Fish Audio synthesis", HttpStatus.BAD_REQUEST);
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("text", text);
        payload.put("reference_id", referenceId);
        payload.put("format", "mp3");

        try {
            byte[] audio = webClient.post()
                    .uri(resolveBaseUrl(credentials) + "/v1/tts")
                    .header(AUTHORIZATION_HEADER, bearerToken(apiKey))
                    .header(MODEL_HEADER, resolveModel(credentials))
                    .accept(MediaType.valueOf("audio/mpeg"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .block();

            if (audio == null || audio.length == 0) {
                throw new ServiceException("Fish Audio returned empty audio", HttpStatus.BAD_GATEWAY);
            }
            return audio;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Fish Audio synthesis failed: " + e.getMessage(),
                    HttpStatus.BAD_GATEWAY, e);
        }
    }

    public String getModelState(String modelId, ResolvedProviderCredentials credentials) {
        String apiKey = requireApiKey(credentials);
        try {
            JsonNode response = webClient.get()
                    .uri(resolveBaseUrl(credentials) + "/model/" + modelId)
                    .header(AUTHORIZATION_HEADER, bearerToken(apiKey))
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .block();
            if (response == null) {
                return null;
            }
            JsonNode state = response.path("state");
            if (!state.isMissingNode() && !state.isNull()) {
                return state.asText();
            }
            return response.path("data").path("state").asText(null);
        } catch (Exception e) {
            log.warn("Failed to fetch Fish Audio model state for {}", modelId, e);
            return null;
        }
    }

    /**
     * Deletes a previously created Fish Audio voice model.
     * HTTP 404 is treated as success (model already removed).
     */
    public void deleteModel(String modelId, ResolvedProviderCredentials credentials) {
        String apiKey = requireApiKey(credentials);
        if (modelId == null || modelId.isBlank()) {
            throw new ServiceException("modelId is required for Fish Audio delete", HttpStatus.BAD_REQUEST);
        }

        try {
            webClient.delete()
                    .uri(resolveBaseUrl(credentials) + "/model/" + modelId)
                    .header(AUTHORIZATION_HEADER, bearerToken(apiKey))
                    .retrieve()
                    .toBodilessEntity()
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .block();
            log.info("Deleted Fish Audio modelId={}", modelId);
        } catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.info("Fish Audio modelId={} already deleted (404)", modelId);
                return;
            }
            throw new ServiceException("Fish Audio model delete failed: " + e.getStatusCode().value()
                    + " " + e.getStatusText() + " body=" + truncate(e.getResponseBodyAsString(), 300),
                    HttpStatus.BAD_GATEWAY, e);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("Fish Audio model delete failed: " + e.getMessage(),
                    HttpStatus.BAD_GATEWAY, e);
        }
    }

    private void waitUntilTrained(String modelId, ResolvedProviderCredentials credentials) {
        String state = getModelState(modelId, credentials);
        if (state == null || "trained".equalsIgnoreCase(state)) {
            return;
        }
        if ("failed".equalsIgnoreCase(state)) {
            throw new ServiceException("Fish Audio voice model training failed", HttpStatus.BAD_GATEWAY);
        }

        int maxPolls = 12;
        for (int i = 0; i < maxPolls; i++) {
            sleep(2000);
            state = getModelState(modelId, credentials);
            if (state == null || "trained".equalsIgnoreCase(state)) {
                return;
            }
            if ("failed".equalsIgnoreCase(state)) {
                throw new ServiceException("Fish Audio voice model training failed", HttpStatus.BAD_GATEWAY);
            }
        }
        throw new ServiceException("Fish Audio voice model is not ready yet; try again shortly",
                HttpStatus.CONFLICT);
    }

    private String extractModelId(JsonNode response) {
        if (response == null) {
            throw new ServiceException("Fish Audio did not return a model id", HttpStatus.BAD_GATEWAY);
        }
        if (response.hasNonNull("_id")) {
            return response.get("_id").asText();
        }
        if (response.hasNonNull("id")) {
            return response.get("id").asText();
        }
        JsonNode data = response.path("data");
        if (data.hasNonNull("_id")) {
            return data.get("_id").asText();
        }
        if (data.hasNonNull("id")) {
            return data.get("id").asText();
        }
        throw new ServiceException("Fish Audio did not return a model id", HttpStatus.BAD_GATEWAY);
    }

    private String requireApiKey(ResolvedProviderCredentials credentials) {
        if (credentials != null && credentials.hasApiKey()) {
            return credentials.getApiKey().trim();
        }
        if (credentials != null && credentials.getApiSecret() != null && !credentials.getApiSecret().isBlank()) {
            return credentials.getApiSecret().trim();
        }
        throw new ServiceException("Fish Audio API key is not configured", HttpStatus.SERVICE_UNAVAILABLE);
    }

    private String bearerToken(String apiKey) {
        return "Bearer " + apiKey;
    }

    private String resolveBaseUrl(ResolvedProviderCredentials credentials) {
        String url = credentials != null && credentials.hasBaseUrl() ? credentials.getBaseUrl() : baseUrl;
        return normalizeFishAudioBaseUrl(url);
    }

    /**
     * {@code https://fish.audio} is the marketing site and permanently redirects (308)
     * to {@code https://api.fish.audio}. WebClient does not follow POST redirects, so
     * a stored website URL would fail clone/TTS. Rewrite that host to the API host.
     */
    static String normalizeFishAudioBaseUrl(String url) {
        if (url == null || url.isBlank()) {
            return "https://api.fish.audio";
        }
        String trimmed = url.trim().replaceAll("/$", "");
        return trimmed.replaceFirst("(?i)^https?://(www\\.)?fish\\.audio(?=/|$)", "https://api.fish.audio");
    }

    private String resolveModel(ResolvedProviderCredentials credentials) {
        return credentials != null && credentials.hasModelName() ? credentials.getModelName().trim() : ttsModel;
    }

    private String truncate(String value, int max) {
        if (value == null) {
            return "";
        }
        return value.length() <= max ? value : value.substring(0, max);
    }

    private void sleep(long delayMs) {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServiceException("Fish Audio poll interrupted", HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }
}
