# Use Cases — Detailed Specification (v2)

## UC-001: User Login with MFA Challenge
| Element | Detail |
|---------|--------|
| **Flow** | 1. Submit username+password → 2. Validate credentials → 3. If MFA enabled, return `mfaToken` + `mfaRequired: true` → 4. Submit mfaToken + 6-digit code → 5. Verify TOTP → 6. Generate tokens |
| **Alt Flow** | MFA disabled → Skip step 3-5, return tokens directly |
| **Exception** | MFA code invalid → 3 remaining attempts → return 401 |
| **Exception** | MFA code 4th invalid → session invalidated, require re-login |
| **Business Rule** | TOTP valid for 30s window, 1-step drift tolerance |

## UC-002: MFA Setup
| Element | Detail |
|---------|--------|
| **Main Flow** | 1. POST /mfa/setup → 2. Generate secret + QR URI → 3. User scans QR with authenticator app → 4. POST /mfa/verify with 6-digit code → 5. Validate → 6. Secret encrypted + saved |
| **Exception** | Code invalid → return 400, retry |
| **Business Rule** | Secret encrypted via EncryptionUtil before DB storage |

## UC-003: Per-tenant Rate Limiting
| Element | Detail |
|---------|--------|
| **Main Flow** | 1. Request arrives at gateway → 2. Extract tenantId → 3. Check tenant rate limit window → 4. If under limit, forward → 5. If over limit, return 429 |
| **Exception** | No tenantId → apply default global limit (lower) |
| **Business Rule** | Limits configurable per plan tier: Trial=50/min, Basic=200/min, Pro=500/min, Enterprise=2000/min |

## UC-004: Trial Expiry
| Element | Detail |
|---------|--------|
| **Main Flow** | 1. Trial activated on registration → 2. Daily job checks expiry → 3. Warning notification at T-3d → 4. Warning at T-1d → 5. At T+0d, feature limits enforced (new tickets blocked, max agents reduced) → 6. 7-day grace period → 7. Tenant suspended |
| **Exception** | User converts to paid → Trial cancelled, full access restored |

## UC-005: Structured Logging
| Element | Detail |
|---------|--------|
| **Main Flow** | 1. Request enters service → 2. MdcLoggingFilter sets correlationId + tenantId + userId → 3. Logback encoder outputs JSON → 4. Filebeat ships to Elasticsearch |
| **Exception** | No correlationId header → Auto-generate UUID |
| **Business Rule** | All log entries include: @timestamp, level, service, traceId, spanId, tenantId, userId, correlationId |
