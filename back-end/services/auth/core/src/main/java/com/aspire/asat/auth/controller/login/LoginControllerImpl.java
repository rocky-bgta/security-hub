package com.aspire.asat.auth.controller.login;

import com.aspire.asat.auth.controller.base.BaseController;
import com.aspire.asat.auth.dto.TokenShortResponse;
import com.aspire.asat.auth.dto.UserDetailsResponse;
import com.aspire.asat.auth.dto.apiResponses.ApiResponse;
import com.aspire.asat.auth.dto.enums.ResponseMessage;
import com.aspire.asat.auth.model.token.ForceLogoutRequest;
import com.aspire.asat.auth.model.token.LoginWithPasswordRequest;
import com.aspire.asat.auth.model.token.LogoutRequest;
import com.aspire.asat.auth.model.token.RefreshTokenRequest;
import com.aspire.asat.auth.service.AccessTokenService;
import com.aspire.asat.auth.service.UserService;
import com.aspire.asat.auth.util.ResponseUtils;
import com.aspire.asat.common.dto.files.CloudFrontCookiesResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class LoginControllerImpl extends BaseController implements LoginController {
    private final AccessTokenService tokenService;
    private final UserService userService;


    @Override
    public ResponseEntity<ApiResponse<TokenShortResponse>> login(LoginWithPasswordRequest request, HttpServletResponse response
    ) {
        TokenShortResponse  accessTokenResponse = tokenService.loginWithUsernamePassword(request);
        // Only set cookies when access token is present and MFA is not required (no tempToken)
        // When MFA is required, cookies will be set after MFA verification completes
        if (accessTokenResponse != null &&
            accessTokenResponse.getAccessToken() != null &&
            (accessTokenResponse.getTempToken() == null || accessTokenResponse.getTempToken().trim().isEmpty())) {
            setSignedCookies(response);
        }

        return ResponseUtils.createSuccessResponseObject(getMessage(ResponseMessage.OPERATION_SUCCESSFUL), accessTokenResponse);
    }

    @Override
    public ResponseEntity<ApiResponse<TokenShortResponse>> refreshToken(RefreshTokenRequest request) {
        return ResponseUtils.createSuccessResponseObject(getMessage(ResponseMessage.OPERATION_SUCCESSFUL), tokenService.refreshToken(request));
    }

    @Override
    public ResponseEntity<ApiResponse<Boolean>> logout(LogoutRequest logoutRequest, HttpServletResponse response) {
        clearSignedCookies(response);
        return ResponseUtils.createSuccessResponseObject(
                getMessage(ResponseMessage.OPERATION_SUCCESSFUL), 
                tokenService.logout(logoutRequest));
    }

    @Override
    public ResponseEntity<ApiResponse<Boolean>> forceLogout(ForceLogoutRequest forceLogoutRequest) {
        return ResponseUtils.createSuccessResponseObject(
                getMessage(ResponseMessage.OPERATION_SUCCESSFUL),
                tokenService.forceLogout(forceLogoutRequest));
    }

    @Override
    public ResponseEntity<ApiResponse<UserDetailsResponse>> getUserDetails() {
        UserDetailsResponse userDetails = userService.getCurrentUserDetails();
        return ResponseUtils.createSuccessResponseObject(getMessage(ResponseMessage.OPERATION_SUCCESSFUL), userDetails);
    }



    public ResponseEntity<ApiResponse<CloudFrontCookiesResponse>> setSignedCookiesOld(HttpServletResponse response) {
        try {
            if (fileService == null || fileProps == null) {
                return ResponseEntity.internalServerError()
                        .body(new ApiResponse<>(
                                "FileService or FileProps not available",
                                500,
                                null
                        ));
            }
            Map<String, String> cookies = fileService.generateSignedCookies();
            String cookieDomain = fileProps.getAws().getCloudFront().getDistributionMainDomain();

            cookies.forEach((name, value) -> {
                ResponseCookie cookie = ResponseCookie.from(name, value)
                        .domain(cookieDomain) // exact subdomain
                        .path("/")
                        .secure(true)
                        .httpOnly(true)
                        .sameSite("None")
                        .maxAge(2 * 60 * 60)
                        .build();

                response.addHeader("Set-Cookie", cookie.toString());
            });

            CloudFrontCookiesResponse responseData = CloudFrontCookiesResponse.builder()
                    .cloudFrontPolicy(cookies.get("CloudFront-Policy"))
                    .cloudFrontSignature(cookies.get("CloudFront-Signature"))
                    .cloudFrontKeyPairId(cookies.get("CloudFront-Key-Pair-Id"))
                    .build();

            return ResponseEntity.ok(new ApiResponse<>(
                    "CloudFront signed cookies generated",
                    200,
                    responseData
            ));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse<>(
                            "Failed to generate signed cookies: " + e.getMessage(),
                            500,
                            null
                    ));
        }
    }
}
