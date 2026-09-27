package com.aspire.asat.auth.controller.login;

import com.aspire.asat.auth.dto.TokenShortResponse;
import com.aspire.asat.auth.dto.UserDetailsResponse;
import com.aspire.asat.auth.dto.apiResponses.ApiResponse;
import com.aspire.asat.auth.model.token.ForceLogoutRequest;
import com.aspire.asat.auth.model.token.LoginWithPasswordRequest;
import com.aspire.asat.auth.model.token.LogoutRequest;
import com.aspire.asat.auth.model.token.RefreshTokenRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import static com.aspire.asat.auth.constant.WebApiUrlConstants.API_BASE_URL;

@Tag(name = "Authentication", description = "Endpoints for user login operations")
@RequestMapping(value = API_BASE_URL, produces = "application/json")
public interface LoginController {

    @PostMapping("/login")
    @Operation(summary = "Login with Password", description = "Authenticate user with username and password")
    ResponseEntity<ApiResponse<TokenShortResponse>> login(@RequestBody @Valid LoginWithPasswordRequest loginWithPasswordRequest, HttpServletResponse response);

    @PostMapping("/refresh")
    @Operation(summary = "Refresh Token", description = "Refresh access token using refresh token")
    ResponseEntity<ApiResponse<TokenShortResponse>> refreshToken(@RequestBody @Valid RefreshTokenRequest refreshTokenRequest);

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Logout the user and invalidate the session. Device information will be saved in activity log. If username is provided, logout that specific user, otherwise logout the current user from token.")
    ResponseEntity<ApiResponse<Boolean>> logout(@RequestBody(required = false) LogoutRequest logoutRequest, HttpServletResponse response);

    @PostMapping("/force-logout")
    @Operation(summary = "Force Logout", description = "Force-logout a user by userId (service-to-service). Revokes Redis access session and refresh tokens without requiring the target user's CurrentContext. Used when admin sets status to INACTIVE, SUSPEND, or blocked.")
    ResponseEntity<ApiResponse<Boolean>> forceLogout(@RequestBody @Valid ForceLogoutRequest forceLogoutRequest);

    @GetMapping("/user-details")
    @Operation(summary = "Get Current User Details", description = "Retrieve current user details from JWT token context")
    ResponseEntity<ApiResponse<UserDetailsResponse>> getUserDetails();

}
