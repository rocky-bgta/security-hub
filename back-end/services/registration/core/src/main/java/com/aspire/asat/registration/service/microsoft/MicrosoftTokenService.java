package com.aspire.asat.registration.service.microsoft;

import com.aspire.asat.registration.data.microsoft.MicrosoftTokenResponseDto;

public interface MicrosoftTokenService {

    MicrosoftTokenResponseDto exchangeCodeForTokens(String authorizationCode);

    MicrosoftTokenResponseDto refreshAccessToken(String refreshToken);
}

