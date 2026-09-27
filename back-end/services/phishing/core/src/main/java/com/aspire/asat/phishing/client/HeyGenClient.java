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
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Thin client for the HeyGen v3 Image-to-Video generation API.
 *
 * <ul>
 *   <li>{@code POST /v3/assets} - upload audio assets</li>
 *   <li>{@code POST /v3/videos} - submit an image-to-video render job ({@code type: "image"})</li>
 *   <li>{@code GET /v3/videos/{id}} - poll job status</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class HeyGenClient {

    private static final String API_KEY_HEADER = "x-api-key";

    private final WebClient webClient;

    @Value("${heygen.base-url:https://api.heygen.com}")
    private String baseUrl;

    @Value("${heygen.timeout-seconds:120}")
    private long timeoutSeconds;

    public HeyGenAssetUploadResult uploadAsset(File file, String contentType, ResolvedProviderCredentials credentials) {
        String apiKey = requireApiKey(credentials);
        if (file == null || !file.exists()) {
            throw new ServiceException("File is required for HeyGen asset upload", HttpStatus.BAD_REQUEST);
        }

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        var part = builder.part("file", new FileSystemResource(file));
        if (contentType != null && !contentType.isBlank()) {
            part.header("Content-Type", contentType);
        }

        try {
            JsonNode response = webClient.post()
                    .uri(resolveBaseUrl(credentials) + "/v3/assets")
                    .header(API_KEY_HEADER, apiKey)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .block();

            JsonNode data = response == null ? null : response.path("data");
            if (data == null || !data.hasNonNull("asset_id")) {
                throw new ServiceException("HeyGen did not return an asset_id", HttpStatus.BAD_GATEWAY);
            }
            return HeyGenAssetUploadResult.builder()
                    .assetId(data.get("asset_id").asText())
                    .url(data.path("url").asText(null))
                    .mimeType(data.path("mime_type").asText(null))
                    .build();
        } catch (ServiceException e) {
            throw e;
        } catch (WebClientResponseException e) {
            throw new ServiceException("HeyGen asset upload failed: " + e.getStatusCode().value()
                    + " " + e.getStatusText() + " body=" + truncate(e.getResponseBodyAsString(), 300),
                    HttpStatus.BAD_GATEWAY, e);
        } catch (Exception e) {
            throw new ServiceException("HeyGen asset upload failed: " + e.getMessage(),
                    HttpStatus.BAD_GATEWAY, e);
        }
    }

    public String createImageVideo(HeyGenImageVideoRequest request, ResolvedProviderCredentials credentials) {
        requireApiKey(credentials);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "image");
        payload.put("image", buildImageInput(request));

        boolean hasAudioAsset = request.getAudioAssetId() != null && !request.getAudioAssetId().isBlank();
        boolean hasAudioUrl = request.getAudioUrl() != null && !request.getAudioUrl().isBlank();
        if (hasAudioAsset) {
            payload.put("audio_asset_id", request.getAudioAssetId());
        } else if (hasAudioUrl) {
            payload.put("audio_url", request.getAudioUrl());
        } else {
            throw new ServiceException("HeyGen video requires audio_asset_id or audio_url", HttpStatus.BAD_REQUEST);
        }

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            payload.put("title", request.getTitle());
        }
        if (request.getResolution() != null && !request.getResolution().isBlank()) {
            payload.put("resolution", request.getResolution());
        }
        if (request.getAspectRatio() != null && !request.getAspectRatio().isBlank()) {
            payload.put("aspect_ratio", request.getAspectRatio());
        }
        if (request.getExpressiveness() != null && !request.getExpressiveness().isBlank()) {
            payload.put("expressiveness", request.getExpressiveness());
        }
        if (request.getMotionPrompt() != null && !request.getMotionPrompt().isBlank()) {
            payload.put("motion_prompt", request.getMotionPrompt());
        }
        if (request.getBackground() != null && !request.getBackground().isEmpty()) {
            payload.put("background", request.getBackground());
            if (request.isRemoveBackground()) {
                payload.put("remove_background", true);
            }
        }

        return submitVideoRequest(payload, credentials);
    }

    private Map<String, Object> buildImageInput(HeyGenImageVideoRequest request) {
        if (request.getImageAssetId() != null && !request.getImageAssetId().isBlank()) {
            Map<String, Object> image = new LinkedHashMap<>();
            image.put("type", "asset_id");
            image.put("asset_id", request.getImageAssetId());
            return image;
        }
        if (request.getImageBytes() != null && request.getImageBytes().length > 0) {
            Map<String, Object> image = new LinkedHashMap<>();
            image.put("type", "base64");
            image.put("media_type", request.getImageMediaType() != null && !request.getImageMediaType().isBlank()
                    ? request.getImageMediaType() : "image/jpeg");
            image.put("data", Base64.getEncoder().encodeToString(request.getImageBytes()));
            return image;
        }
        if (request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
            Map<String, Object> image = new LinkedHashMap<>();
            image.put("type", "url");
            image.put("url", request.getImageUrl());
            return image;
        }
        throw new ServiceException("HeyGen image video requires image asset_id, bytes, or url",
                HttpStatus.BAD_REQUEST);
    }

    private String submitVideoRequest(Map<String, Object> payload, ResolvedProviderCredentials credentials) {
        String apiKey = requireApiKey(credentials);
        Object imageInput = payload.get("image");
        Object imageType = imageInput instanceof Map<?, ?> imageMap ? imageMap.get("type") : null;
        log.debug("Submitting HeyGen video request type={} imageInputType={}",
                payload.get("type"), imageType);
        try {
            JsonNode response = webClient.post()
                    .uri(resolveBaseUrl(credentials) + "/v3/videos")
                    .header(API_KEY_HEADER, apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .block();

            JsonNode data = response == null ? null : response.path("data");
            if (data == null || !data.hasNonNull("video_id")) {
                throw new ServiceException("HeyGen did not return a video_id", HttpStatus.BAD_GATEWAY);
            }
            return data.get("video_id").asText();
        } catch (ServiceException e) {
            throw e;
        } catch (WebClientResponseException e) {
            throw new ServiceException("HeyGen video generation failed: " + e.getStatusCode().value()
                    + " " + e.getStatusText() + " body=" + truncate(e.getResponseBodyAsString(), 300),
                    HttpStatus.BAD_GATEWAY, e);
        } catch (Exception e) {
            throw new ServiceException("HeyGen video generation failed: " + e.getMessage(),
                    HttpStatus.BAD_GATEWAY, e);
        }
    }

    /**
     * @return the polled job node containing {@code status} and (when ready) {@code video_url}
     */
    public JsonNode getVideoStatus(String videoId, ResolvedProviderCredentials credentials) {
        String apiKey = requireApiKey(credentials);
        try {
            JsonNode response = webClient.get()
                    .uri(resolveBaseUrl(credentials) + "/v3/videos/" + videoId)
                    .header(API_KEY_HEADER, apiKey)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .block();
            if (response == null) {
                throw new ServiceException("HeyGen returned empty status", HttpStatus.BAD_GATEWAY);
            }
            return response.path("data");
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("HeyGen status poll failed: " + e.getMessage(),
                    HttpStatus.BAD_GATEWAY, e);
        }
    }

    public byte[] downloadVideo(String url) {
        return downloadBinary(url, "video");
    }

    public byte[] downloadBinary(String url) {
        return downloadBinary(url, "asset");
    }

    private byte[] downloadBinary(String url, String what) {
        try {
            byte[] bytes = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .block();
            if (bytes == null || bytes.length == 0) {
                throw new ServiceException("HeyGen returned empty " + what, HttpStatus.BAD_GATEWAY);
            }
            return bytes;
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("HeyGen " + what + " download failed: " + e.getMessage(),
                    HttpStatus.BAD_GATEWAY, e);
        }
    }

    private String requireApiKey(ResolvedProviderCredentials credentials) {
        if (credentials != null && credentials.hasApiKey()) {
            return credentials.getApiKey().trim();
        }
        if (credentials != null && credentials.getApiSecret() != null && !credentials.getApiSecret().isBlank()) {
            return credentials.getApiSecret().trim();
        }
        throw new ServiceException("HeyGen API key is not configured", HttpStatus.SERVICE_UNAVAILABLE);
    }

    private String resolveBaseUrl(ResolvedProviderCredentials credentials) {
        String url = credentials != null && credentials.hasBaseUrl() ? credentials.getBaseUrl() : baseUrl;
        return resolve(url);
    }

    private String resolve(String url) {
        return url == null ? "" : url.trim().replaceAll("/$", "");
    }

    private String truncate(String value, int max) {
        if (value == null) {
            return "";
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}
