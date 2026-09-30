# Auth Service Access Token Flow

Swagger UI:

```text
http://localhost:9093/auth/swagger-ui/index.html
```

Base URL:

```text
http://localhost:9093/auth
```

Use the following API calls in order when MFA is enabled. The login API first returns a temporary token. Then the temporary token is passed into the MFA OTP APIs. After OTP verification, the response contains the real access token.

## 1. Login and Get Temporary Token

Endpoint:

```http
POST /api/v1/auth/login
```

Full URL:

```http
POST http://localhost:9093/auth/api/v1/auth/login
```

Payload:

```json
{
  "username": "dummy",
  "password": "123456",
  "deviceInfo": {
    "platformType": "WEB",
    "platformInfo": "GOOGLE CHROME",
    "platformVersion": "1001.0.1.1",
    "deviceIdentifier": "42345245",
    "appLanguage": "ENGLISH",
    "appVersion": "1.0.1"
  }
}
```

Expected MFA response shape:

```json
{
  "message": "Operation successful",
  "statusCode": 200,
  "data": {
    "mfaSetupRequired": false,
    "mfaVerificationRequired": true,
    "mfaMethod": "EMAIL",
    "methods": [],
    "tempToken": "TEMP_TOKEN_HERE"
  }
}
```

Save:

```text
data.tempToken
```

If `data.accessToken` is already present, MFA is not required and no further OTP call is needed.

## 2. Generate OTP

Endpoint:

```http
POST /api/v1/auth/mfa/generate-otp
```

Full URL:

```http
POST http://localhost:9093/auth/api/v1/auth/mfa/generate-otp
```

Payload:

```json
{
  "method": "EMAIL",
  "tempToken": "TEMP_TOKEN_HERE"
}
```

Allowed `method` values from Swagger:

```text
EMAIL
AUTHENTICATOR
SMS
PHONE_CALL
```

For normal email OTP login, use `EMAIL`. For SMS OTP login, use `SMS`.

Expected response shape:

```json
{
  "message": "Operation successful",
  "statusCode": 200,
  "data": {
    "sessionId": "SESSION_ID_HERE",
    "expiresIn": 300,
    "phoneNumber": null,
    "resendCooldownSeconds": 60,
    "resendsRemaining": 3,
    "attemptsRemaining": 5
  }
}
```

Save:

```text
data.sessionId
```

## 3. Verify OTP and Get Access Token

Endpoint:

```http
POST /api/v1/auth/mfa/verify-otp
```

Full URL:

```http
POST http://localhost:9093/auth/api/v1/auth/mfa/verify-otp
```

Payload for `EMAIL`, `SMS`, or `PHONE_CALL`:

```json
{
  "method": "EMAIL",
  "code": "123456",
  "sessionId": "SESSION_ID_HERE",
  "tempToken": "TEMP_TOKEN_HERE"
}
```

Expected success response shape:

```json
{
  "message": "Operation successful",
  "statusCode": 200,
  "data": {
    "verified": true,
    "message": "OTP verified successfully",
    "tokenResponse": {
      "accessToken": "ACCESS_TOKEN_HERE",
      "refreshToken": "REFRESH_TOKEN_HERE",
      "deviceBindingNeeded": false,
      "credentialChangeNeeded": false,
      "lastLoginTime": "2026-09-30T00:00:00Z",
      "roleNames": [],
      "trialDaysLeft": 0,
      "isBuyNow": false,
      "onboardBy": null
    }
  }
}
```

Save:

```text
data.tokenResponse.accessToken
data.tokenResponse.refreshToken
```

Use the access token in protected API calls:

```http
Authorization: Bearer ACCESS_TOKEN_HERE
```

## 4. Resend OTP When Needed

Endpoint:

```http
POST /api/v1/auth/mfa/resend-otp
```

Full URL:

```http
POST http://localhost:9093/auth/api/v1/auth/mfa/resend-otp
```

Payload:

```json
{
  "method": "EMAIL",
  "tempToken": "TEMP_TOKEN_HERE"
}
```

The response shape is the same as generate OTP and contains a new or active `sessionId`.

## 5. Authenticator App Flow

If the MFA method is `AUTHENTICATOR`, do not call `generate-otp`. Verify the authenticator app code directly.

Endpoint:

```http
POST /api/v1/auth/mfa/verify-otp
```

Payload:

```json
{
  "method": "AUTHENTICATOR",
  "code": "123456",
  "tempToken": "TEMP_TOKEN_HERE"
}
```

For authenticator verification, `sessionId` is not required.

## 6. Refresh Access Token

Endpoint:

```http
POST /api/v1/auth/refresh
```

Full URL:

```http
POST http://localhost:9093/auth/api/v1/auth/refresh
```

Payload:

```json
{
  "refreshToken": "REFRESH_TOKEN_HERE",
  "deviceInfo": {
    "platformType": "WEB",
    "platformInfo": "GOOGLE CHROME",
    "platformVersion": "1001.0.1.1",
    "deviceIdentifier": "42345245",
    "appLanguage": "ENGLISH",
    "appVersion": "1.0.1"
  }
}
```

Expected response contains a new token response:

```json
{
  "message": "Operation successful",
  "statusCode": 200,
  "data": {
    "accessToken": "NEW_ACCESS_TOKEN_HERE",
    "refreshToken": "NEW_REFRESH_TOKEN_HERE"
  }
}
```

## Quick Sequence

```text
1. POST /api/v1/auth/login
   -> copy data.tempToken

2. POST /api/v1/auth/mfa/generate-otp
   -> pass tempToken
   -> copy data.sessionId

3. POST /api/v1/auth/mfa/verify-otp
   -> pass method, code, sessionId, tempToken
   -> copy data.tokenResponse.accessToken

4. Use Authorization: Bearer ACCESS_TOKEN_HERE
```
হ্যাঁ, তোমার ওই Auth Swagger-এ access token পেতে তিনটা কল পরপর দিতে হবে। আগে POST /api/v1/auth/login-এ username, password আর deviceInfo দেবে—এতে tempToken আসবে। তারপর POST /api/v1/auth/mfa/generate-otp-
হ্যাঁ। তোমার ওই Auth Swagger-এ access token পাওয়ার জন্য ৩টা API ধারাবাহিকভাবে hit করতে হবে:
1. POST /api/v1/auth/login
   Username/password এবং deviceInfo দেবে। Response-এ tempToken পাবে। এটা final access token না।
2. POST /api/v1/auth/mfa/generate-otp
   এখানে method = EMAIL এবং আগের tempToken দেবে। এরপর email-এ OTP যাবে এবং response-এ sessionId পাবে।
3. POST /api/v1/auth/mfa/verify-otp
   এখানে আগের tempToken + sessionId + email-এ পাওয়া OTP code দেবে। Successful হলে response-এ final accessToken এবং refreshToken পাবে।
   তোমার local Auth Swagger ছিল http://localhost:9093/auth/swagger-ui.html। তাই Swagger খুলে sequenceটা মনে রাখো:
   login → generate-otp → verify-otp → accessToken
   চাইলে এখন আমরা প্রথম /login endpoint থেকেই আস্তে আস্তে শুরু করতে পারি—Swagger-এ কোন field-এ কী value দিতে হবে সেটা একটার পর একটা।
   এ method = EMAIL এবং আগের tempToken পাঠাবে। তাতে তোমার ইমেইলে একটা OTP যাবে, আর response-এ sessionId পাবে। শেষে POST /api/v1/auth/mfa/verify-otp-তে ওই tempToken, sessionId, আর ইমেইলে পাওয়া OTP কোড পাঠাবে—সফল হলে তখনই ফাইনাল accessToken এবং refreshToken পেয়ে যাবে। ইচ্ছা করলে এখন আমরা একদম প্রথম login endpoint থেকেই আস্তে আস্তে শুরু করতে পারি, ধাপে ধাপে দেখে নেব কোন ফিল্ডে কী ভ্যালু দিতে হবে।