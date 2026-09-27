package com.aspire.asat.registration.service.microsoft.impl;

import com.aspire.asat.registration.config.MicrosoftEntraConfig;
import com.aspire.asat.registration.data.microsoft.MicrosoftTokenResponseDto;
import com.aspire.asat.registration.service.microsoft.MicrosoftTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
@RequiredArgsConstructor
public class MicrosoftTokenServiceImpl implements MicrosoftTokenService {

    private final MicrosoftEntraConfig entraConfig;
    private final WebClient webClient = WebClient.builder().build();

    @Override
    public MicrosoftTokenResponseDto exchangeCodeForTokens(String authorizationCode) {
        log.info("Exchanging authorization code for tokens");

        String tokenUrl = "https://login.microsoftonline.com/"
                + entraConfig.getTenantId() + "/oauth2/v2.0/token";

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", entraConfig.getClientId());
        formData.add("client_secret", entraConfig.getClientSecret());
        formData.add("code", authorizationCode);
        formData.add("redirect_uri", entraConfig.getRedirectUri());
        formData.add("grant_type", "authorization_code");
        formData.add("scope", entraConfig.getScopes());

        MicrosoftTokenResponseDto response = webClient.post()
                .uri(tokenUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(MicrosoftTokenResponseDto.class)
                .block();

        log.info("Token exchange successful");
        return response;
    }

    @Override
    public MicrosoftTokenResponseDto refreshAccessToken(String refreshToken) {
        log.info("Refreshing access token");

        String tokenUrl = "https://login.microsoftonline.com/"
                + entraConfig.getTenantId() + "/oauth2/v2.0/token";

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", entraConfig.getClientId());
        formData.add("client_secret", entraConfig.getClientSecret());
        formData.add("refresh_token", refreshToken);
        formData.add("grant_type", "refresh_token");
        formData.add("scope", entraConfig.getScopes());

        MicrosoftTokenResponseDto response = webClient.post()
                .uri(tokenUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(MicrosoftTokenResponseDto.class)
                .block();

        log.info("Token refresh successful");
        return response;
    }
}

