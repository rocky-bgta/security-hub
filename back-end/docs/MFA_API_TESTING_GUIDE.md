# MFA API Testing Guide

This guide explains how to test the Multi-Factor Authentication (MFA) APIs using the Postman collection.

## Prerequisites

1. **Postman** installed (or use any REST client)
2. **MFA Configuration** enabled in `application.yml`:
   ```yaml
   mfa:
     enabled: true
     sms-enabled: true
     email-enabled: true
     authenticator-enabled: true
   ```
3. **User Account** with valid credentials
4. **Access Token** from login endpoint

## Setup Instructions

### 1. Import Postman Collection

1. Open Postman
2. Click **Import** button
3. Select `MFA_API_POSTMAN_COLLECTION.json`
4. The collection will be imported with all endpoints

### 2. Configure Environment Variables

Create a new Postman Environment or use the default one with these variables:

| Variable | Description | Example Value |
|----------|-------------|---------------|
| `baseUrl` | Base URL of the auth service | `http://localhost:9093` |
| `username` | Your username/email | `user@example.com` |
| `password` | Your password | `your-password` |
| `phoneNumber` | Phone number for SMS (optional; with country code). If omitted, user profile phone is used | `+1234567890` |
| `accessToken` | JWT access token (auto-filled after login) | (auto-filled) |
| `mfaSessionId` | OTP session ID (auto-filled after OTP generation) | (auto-filled) |
| `currentContext` | Base64 encoded user context (auto-generated) | (auto-generated) |

### 3. Understanding the Flow

#### Flow 1: SMS/Email MFA Setup and Verification

1. **Login** → Get access token
2. **Generate OTP (SMS/Email)** → Receive OTP code via SMS/Email
3. **Verify OTP** → Complete MFA verification

#### Flow 2: Authenticator App Setup

1. **Login** → Get access token
2. **Setup Authenticator** → Get QR code and secret
3. **Scan QR code** in authenticator app (Google Authenticator, Microsoft Authenticator, etc.)
4. **Verify Authenticator Setup** → Enter TOTP code from app to complete setup

## API Endpoints

### 1. Login
**POST** `/auth/api/v1/auth/login`

**Request Body:**
```json
{
    "username": "user@example.com",
    "password": "your-password",
    "deviceInfo": {
        "deviceId": "test-device-123",
        "deviceName": "Postman Test Device",
        "os": "Postman",
        "browser": "Postman"
    }
}
```

**Response:**
```json
{
    "message": "Operation successful",
    "status": 200,
    "data": {
        "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        "refreshToken": "refresh-token-here",
        "mfaSetupRequired": false,
        "mfaVerificationRequired": true,
        "mfaMethod": "SMS"
    }
}
```

**Note:** If `mfaSetupRequired` is `true`, user needs to set up MFA first. If `mfaVerificationRequired` is `true`, user needs to verify MFA code.

---

### 2. Generate OTP (SMS)
**POST** `/auth/api/v1/auth/mfa/generate-otp`

**Headers:**
- `Authorization: Bearer {accessToken}`
- `CurrentContext: {base64-encoded-context}`

**Request Body:**
```json
{
    "method": "SMS",
    "phoneNumber": "+1234567890"
}
```

`phoneNumber` is optional. If provided, OTP is sent to that number. If omitted, OTP is sent to the phone number stored on the user profile. If neither is available, the request fails.

**Response:**
```json
{
    "message": "Operation successful",
    "status": 200,
    "data": {
        "sessionId": "550e8400-e29b-41d4-a716-446655440000",
        "expiresIn": 300
    }
}
```

**Note:** OTP will be sent via SMS. Save the `sessionId` for verification.

---

### 3. Generate OTP (Email)
**POST** `/auth/api/v1/auth/mfa/generate-otp`

**Headers:**
- `Authorization: Bearer {accessToken}`
- `CurrentContext: {base64-encoded-context}`

**Request Body:**
```json
{
    "method": "EMAIL"
}
```

**Response:**
```json
{
    "message": "Operation successful",
    "status": 200,
    "data": {
        "sessionId": "550e8400-e29b-41d4-a716-446655440000",
        "expiresIn": 300
    }
}
```

**Note:** OTP will be sent via Email to user's registered email address.

---

### 4. Verify OTP
**POST** `/auth/api/v1/auth/mfa/verify-otp`

**Headers:**
- `Authorization: Bearer {accessToken}`
- `CurrentContext: {base64-encoded-context}`

**Request Body (SMS/Email):**
```json
{
    "method": "SMS",
    "code": "123456",
    "sessionId": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Request Body (Authenticator):**
```json
{
    "method": "AUTHENTICATOR",
    "code": "123456",
    "sessionId": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Response:**
```json
{
    "message": "Operation successful",
    "status": 200,
    "data": {
        "verified": true,
        "message": "OTP verified successfully"
    }
}
```

---

### 5. Setup Authenticator (Generate QR Code)
**POST** `/auth/api/v1/auth/mfa/authenticator/setup`

**Headers:**
- `Authorization: Bearer {accessToken}`
- `CurrentContext: {base64-encoded-context}`

**Request Body:** (empty)

**Response:**
```json
{
    "message": "Operation successful",
    "status": 200,
    "data": {
        "qrCodeUrl": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...",
        "secret": "JBSWY3DPEHPK3PXP",
        "otpauthUri": "otpauth://totp/ASAT%20Platform:user@example.com?secret=JBSWY3DPEHPK3PXP&issuer=ASAT%20Platform"
    }
}
```

**Steps:**
1. Copy the `secret` or scan the QR code
2. Add account to authenticator app (Google Authenticator, Microsoft Authenticator, Authy, etc.)
3. Use the TOTP code from the app in the next step

---

### 6. Verify Authenticator Setup
**POST** `/auth/api/v1/auth/mfa/authenticator/verify-setup`

**Headers:**
- `Authorization: Bearer {accessToken}`
- `CurrentContext: {base64-encoded-context}`

**Request Body:**
```json
{
    "code": "123456"
}
```

**Response:**
```json
{
    "message": "Operation successful",
    "status": 200,
    "data": true
}
```

**Note:** After successful verification, MFA will be enabled for the user with AUTHENTICATOR method.

---

## Testing Scenarios

### Scenario 1: First-Time MFA Setup (SMS)

1. Login → Check response for `mfaSetupRequired: true`
2. Generate OTP (SMS) → Receive OTP via SMS
3. Verify OTP → Complete setup
4. Login again → Should require MFA verification

### Scenario 2: MFA Verification Flow (Email)

1. Login → Get `mfaVerificationRequired: true` and `mfaMethod: "EMAIL"`
2. Generate OTP (Email) → Check email for OTP code
3. Verify OTP → Complete login

### Scenario 3: Authenticator App Setup

1. Login → Get access token
2. Setup Authenticator → Get QR code
3. Scan QR code in authenticator app
4. Verify Authenticator Setup → Enter TOTP code from app
5. Login again → Use authenticator app for MFA

### Scenario 4: Error Handling

**Test Invalid OTP:**
- Generate OTP
- Verify with wrong code → Should return error
- Try 5 times → Should lock verification

**Test Expired OTP:**
- Generate OTP
- Wait 5+ minutes
- Verify OTP → Should return "OTP expired" error

**Test Invalid Session:**
- Generate OTP
- Use wrong sessionId → Should return error

---

## Common Issues

### Issue: "MFA is not enabled"
**Solution:** Check `application.yml` and ensure `mfa.enabled: true`

### Issue: "MFA method not enabled"
**Solution:** Enable the specific method in config:
- `mfa.sms-enabled: true` for SMS
- `mfa.email-enabled: true` for Email
- `mfa.authenticator-enabled: true` for Authenticator

### Issue: "CurrentContext header missing"
**Solution:** The collection auto-generates this from JWT token. Ensure you've logged in first.

### Issue: "OTP not received"
**Solution:** 
- Check notification service is running
- Verify SMS/Email configuration
- Check user's phone/email in database

### Issue: "Invalid TOTP code"
**Solution:**
- Ensure time sync is correct on device
- Use code from authenticator app (not the secret)
- Try code from previous/next time window (±30 seconds)

---

## Security Notes

1. **Never share OTP codes** - They expire in 5 minutes
2. **Rate limiting** - Maximum 10 OTP requests per hour per user
3. **Failed attempts** - Account locked after 5 failed verification attempts
4. **Encryption** - TOTP secrets are encrypted at rest
5. **Hashing** - OTP codes are hashed using BCrypt before storage

---

## Additional Resources

- **Swagger UI**: `http://localhost:9093/auth/swagger-ui.html`
- **API Docs**: `http://localhost:9093/auth/v3/api-docs`

---

## Support

For issues or questions, check:
1. Application logs for detailed error messages
2. MongoDB `mfa_codes` collection for OTP records
3. MongoDB `aspire_user` collection for user MFA status


