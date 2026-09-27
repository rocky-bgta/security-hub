package com.aspire.asat.phishing.service.impl;

import com.aspire.asat.phishing.exception.ServiceException;
import com.aspire.asat.phishing.service.OpenAiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.File;
import java.time.Duration;
import java.util.Map;

@Service
@Slf4j
public class OpenAiServiceImpl implements OpenAiService {

    @Value("${openai.base-url:https://api.openai.com/v1}")
    private String baseUrl;

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.model:gpt-4o-transcribe}")
    private String model;

    @Value("${openai.timeout-seconds:60}")
    private long timeoutSeconds;

    @Override
    public String transcribe(File file, String languageHint, String correlationId) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(file));
        body.add("model", model);
        try {
            Map<?, ?> response = WebClient.builder()
                    .baseUrl(baseUrl)
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .build()
                    .post()
                    .uri("/audio/transcriptions")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .block();

            if (response == null || response.get("text") == null) {
                throw new ServiceException("OpenAI response did not contain transcription text", HttpStatus.BAD_GATEWAY);
            }
            logTokenUsage(response, correlationId);
            return response.get("text").toString();
        } catch (WebClientResponseException e) {
            String responseBody = e.getResponseBodyAsString();
            log.error(
                    "OpenAI transcription HTTP {} for audioId={} body={}",
                    e.getStatusCode(),
                    correlationId,
                    responseBody
            );
            throw new ServiceException(
                    "OpenAI transcription call failed: " + responseBody,
                    HttpStatus.BAD_GATEWAY,
                    e
            );
        } catch (ServiceException e) {
            log.error("OpenAI transcription error: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected OpenAI transcription error", e);
            throw new ServiceException("Unable to transcribe audio", HttpStatus.BAD_GATEWAY, e);
        }
    }

    private void logTokenUsage(Map<?, ?> response, String correlationId) {
        Object usageObj = response.get("usage");
        if (!(usageObj instanceof Map<?, ?> usage)) {
            log.info("openai_transcription_usage audioId={} usage=not-provided", correlationId);
            return;
        }

        Object inputTokens = usage.get("input_tokens");
        Object outputTokens = usage.get("output_tokens");
        Object totalTokens = usage.get("total_tokens");

        log.info(
                "openai_transcription_usage audioId={} inputToken={} outputToken={} totalTokens={}",
                correlationId,
                inputTokens == null ? "n/a" : inputTokens,
                outputTokens == null ? "n/a" : outputTokens,
                totalTokens == null ? "n/a" : totalTokens
        );
    }

}
