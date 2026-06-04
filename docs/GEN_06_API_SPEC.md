# API Specification (v2)

## Base URL: `https://api.vcall.com/api/v1`

## Authentication
- `Authorization: Bearer <access_token>` header for all endpoints except `/auth/*`, `/tenants/register`, `/mfa/setup`
- Access token: 15 min expiry, RS256 signed
- Refresh token: 7 day expiry, one-time-use, family rotation with theft detection

---

## Auth Service (`iam-service`)

### POST /auth/login
**Response 200 (MFA NOT enabled)**:
```json
{ "mfaRequired": false, "accessToken": "jwt...", "refreshToken": "jwt...", "expiresIn": 900, "user": {...} }
```
**Response 200 (MFA enabled)**:
```json
{ "mfaRequired": true, "mfaToken": "temp_token_xxx", "expiresIn": 300 }
```

### POST /auth/mfa-challenge
Verify MFA code after receiving mfaToken
**Request**: `{ "mfaToken": "string", "code": "string (6 digits)" }`
**Response 200**: `{ "accessToken": "jwt...", "refreshToken": "jwt...", "expiresIn": 900, "user": {...} }`
**Response 401**: `{ "error": "Invalid code", "remainingAttempts": 2 }`

### POST /auth/refresh
**Request**: `{ "refreshToken": "string" }`
**Response 200**: `{ "accessToken": "string", "refreshToken": "string", "expiresIn": 900 }`
**Response 401**: Token revoked / theft detected (family revoked)

### POST /auth/forgot-password
**Response 200**: `{ "message": "Reset link sent if email exists" }`

### POST /auth/reset-password
**Request**: `{ "token": "string", "newPassword": "string" }`
**Response 200**: `{ "message": "Password reset successful" }`

---

## MFA Service

### POST /mfa/setup
**Response 200**:
```json
{ "secret": "JBSWY3DPEHPK3PXP", "qrCodeUri": "otpauth://totp/...", "enabled": false }
```

### POST /mfa/verify
**Request**: `{ "code": "123456" }`
**Response 200**: `{ "message": "MFA enabled successfully" }`
**Response 400**: `{ "error": "Invalid verification code" }`

### POST /mfa/disable
**Response 200**: `{ "message": "MFA disabled" }`

---

## Tenant Service

### POST /tenants/register
**Request**: `{ "companyName", "adminEmail", "adminName", "password", "phone?", "plan?" }`
**Response 201**:
```json
{
  "tenantId": "tnt_abc123",
  "companyName": "My Clinic",
  "adminUsername": "admin@clinic.com",
  "plan": "TRIAL",
  "trialEndDate": "2026-06-18T10:00:00",
  "maxAgents": 5,
  "maxUsers": 10,
  "status": "ACTIVE",
  "message": "Tenant provisioned successfully. Welcome to VCall Contact Center!"
}
```

### GET /tenants/check?companyName=...
**Response 200**: `{ "success": true, "data": true/false }`

### GET /tenants/{id}/subscription
**Response 200**: Subscription details (plan, status, trial end, usage limits)

### GET /tenants/{id}/usage
**Response 200**: `{ "agentsUsed": 3, "usersUsed": 8, "callsThisMonth": 245, "limits": { "maxAgents": 5, "maxUsers": 10, "maxCallsPerMonth": 500 } }`
