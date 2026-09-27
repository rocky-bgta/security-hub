package com.aspire.asat.phishing.client;

import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.exception.VoiceCloneLimitReachedException;
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
 * Thin client for the ElevenLabs voice cloning + text-to-speech APIs.
 *
 * <ul>
 *   <li>{@code POST /v1/voices/add} - instant voice clone from a sample</li>
 *   <li>{@code POST /v1/text-to-speech/{voiceId}} - synthesize speech</li>
 *   <li>{@code DELETE /v1/voices/{voiceId}} - delete a cloned voice</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ElevenLabsClient {

    private static final String API_KEY_HEADER = "xi-api-key";

    private final WebClient webClient;

    @Value("${elevenlabs.base-url:https://api.elevenlabs.io}")
    private String baseUrl;

    @Value("${elevenlabs.model-id:eleven_multilingual_v2}")
    private String modelId;

    @Value("${elevenlabs.timeout-seconds:120}")
    private long timeoutSeconds;

    public String addVoice(File sampleFile, String name, ResolvedProviderCredentials credentials) {
        String apiKey = requireApiKey(credentials);

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("name", name == null || name.isBlank() ? "deepfake-voice" : name);
        builder.part("files", new FileSystemResource(sampleFile));

        try {
            JsonNode response = webClient.post()
                    .uri(resolveBaseUrl(credentials) + "/v1/voices/add")
                    .header(API_KEY_HEADER, apiKey)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .block();

            if (response == null || !response.hasNonNull("voice_id")) {
                throw new ServiceException("ElevenLabs did not return a voice_id", HttpStatus.BAD_GATEWAY);
            }
            return response.get("voice_id").asText();
        } catch (ServiceException e) {
            throw e;
        } catch (WebClientResponseException e) {
            String body = e.getResponseBodyAsString();
            if (isVoiceLimitReached(body)) {
                throw new VoiceCloneLimitReachedException(
                        "ElevenLabs custom voice limit reached: " + truncate(body, 300), e);
            }
            throw new ServiceException("ElevenLabs voice clone failed: " + e.getStatusCode().value()
                    + " " + e.getStatusText() + " body=" + truncate(body, 300),
                    HttpStatus.BAD_GATEWAY, e);
        } catch (Exception e) {
            throw new ServiceException("ElevenLabs voice clone failed: " + e.getMessage(),
                    HttpStatus.BAD_GATEWAY, e);
        }
    }

    private boolean isVoiceLimitReached(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return false;
        }
        return responseBody.contains("voice_limit_reached");
    }

    public byte[] textToSpeech(String voiceId, String text, String language, ResolvedProviderCredentials credentials) {
        String apiKey = requireApiKey(credentials);
        if (voiceId == null || voiceId.isBlank()) {
            throw new ServiceException("voiceId is required for synthesis", HttpStatus.BAD_REQUEST);
        }

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("text", text);
        payload.put("model_id", credentials != null && credentials.hasModelName()
                ? credentials.getModelName().trim() : modelId);

        try {
            byte[] audio = webClient.post()
                    .uri(resolveBaseUrl(credentials) + "/v1/text-to-speech/" + voiceId)
                    .header(API_KEY_HEADER, apiKey)
                    .accept(MediaType.valueOf("audio/mpeg"))
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .block();

            if (audio == null || audio.length == 0) {
                throw new ServiceException("ElevenLabs returned empty audio", HttpStatus.BAD_GATEWAY);
            }
            return audio;
        } catch (ServiceException e) {
            throw e;
        } catch (WebClientResponseException e) {
            throw new ServiceException("ElevenLabs synthesis failed: " + e.getStatusCode().value()
                    + " " + e.getStatusText() + " body=" + truncate(e.getResponseBodyAsString(), 300),
                    HttpStatus.BAD_GATEWAY, e);
        } catch (Exception e) {
            throw new ServiceException("ElevenLabs synthesis failed: " + e.getMessage(),
                    HttpStatus.BAD_GATEWAY, e);
        }
    }

    /**
     * Deletes a previously cloned voice on ElevenLabs.
     * HTTP 404 is treated as success (voice already removed).
     */
    public void deleteVoice(String voiceId, ResolvedProviderCredentials credentials) {
        String apiKey = requireApiKey(credentials);
        if (voiceId == null || voiceId.isBlank()) {
            throw new ServiceException("voiceId is required for delete", HttpStatus.BAD_REQUEST);
        }

        try {
            webClient.delete()
                    .uri(resolveBaseUrl(credentials) + "/v1/voices/" + voiceId)
                    .header(API_KEY_HEADER, apiKey)
                    .retrieve()
                    .toBodilessEntity()
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .block();
            log.info("Deleted ElevenLabs voiceId={}", voiceId);
        } catch (WebClientResponseException e) {
            String responseBody = e.getResponseBodyAsString();
            if (e.getStatusCode() == HttpStatus.NOT_FOUND || isVoiceNotFound(responseBody)) {
                log.info("ElevenLabs voiceId={} already deleted or not found", voiceId);
                return;
            }
            throw new ServiceException("ElevenLabs voice delete failed: " + e.getStatusCode().value()
                    + " " + e.getStatusText() + " body=" + truncate(responseBody, 300),
                    HttpStatus.BAD_GATEWAY, e);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("ElevenLabs voice delete failed: " + e.getMessage(),
                    HttpStatus.BAD_GATEWAY, e);
        }
    }

    private boolean isVoiceNotFound(String responseBody) {
        return responseBody != null
                && (responseBody.contains("\"code\":\"voice_not_found\"")
                || responseBody.contains("\"status\":\"voice_does_not_exist\""));
    }

    private String requireApiKey(ResolvedProviderCredentials credentials) {
        if (credentials != null && credentials.hasApiKey()) {
            return credentials.getApiKey().trim();
        }
        if (credentials != null && credentials.getApiSecret() != null && !credentials.getApiSecret().isBlank()) {
            return credentials.getApiSecret().trim();
        }
        throw new ServiceException("ElevenLabs API key is not configured", HttpStatus.SERVICE_UNAVAILABLE);
    }

    private String resolveBaseUrl(ResolvedProviderCredentials credentials) {
        String url = credentials != null && credentials.hasBaseUrl() ? credentials.getBaseUrl() : baseUrl;
        return url == null || url.isBlank()
                ? "https://api.elevenlabs.io"
                : url.trim().replaceAll("/$", "");
    }

    private String truncate(String value, int max) {
        if (value == null) {
            return "";
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}
