# Test Cases (v2)

## MFA Tests

### TC-MFA-001: MFA Setup Success
1. `POST /api/v1/mfa/setup` (authenticated)
2. Expect 200 with `secret` (base32) + `qrCodeUri`
3. Verify `enabled: false` initially

### TC-MFA-002: MFA Verify — Valid Code
1. Setup MFA, extract secret
2. Generate TOTP code from secret (via test TOTP library)
3. `POST /api/v1/mfa/verify { code }`
4. Expect 200, user.mfaEnabled=true

### TC-MFA-003: MFA Verify — Invalid Code
1. Setup MFA
2. `POST /api/v1/mfa/verify` with wrong 6-digit code
3. Expect 400

### TC-MFA-004: Login with MFA Enabled
1. Login with password → expect 200, `mfaRequired: true`, `mfaToken`
2. `POST /auth/mfa-challenge` with invalid code → expect 401
3. With valid code → expect 200, accessToken returned

### TC-MFA-005: MFA Disable
1. MFA enabled + verify code
2. `POST /api/v1/mfa/disable`
3. Expect 200, user.mfaEnabled=false

## Per-tenant Rate Limiter Tests

### TC-RL-001: Tenant Under Limit
1. Configure tenant limit 100/min
2. Send 99 requests from tenant
3. All expect 200

### TC-RL-002: Tenant Over Limit
1. Configure tenant limit 50/min
2. Send 51 requests from tenant
3. 51st expects 429

### TC-RL-003: Different Tenants Independent
1. Tenant A limit 50/min, Tenant B limit 50/min
2. Send 60 from Tenant A → 10 expect 429
3. Send 60 from Tenant B → 10 expect 429
4. Verify tenants don't affect each other

## Trial Lifecycle Tests

### TC-TR-001: Trial Activation
1. `POST /tenants/register` with plan=TRIAL
2. Verify response includes `trialEndDate` (14 days from now)
3. Verify `maxAgents=5`, `maxUsers=10`

### TC-TR-002: Trial Expiry Detection
1. Activate trial
2. Simulate 15 days passing (adjust system clock/mock)
3. `trialLifecycleService.isTrialExpired()` returns true

### TC-TR-003: Feature Limits Enforced During Trial
1. Tenant with 5 agent max
2. `FeatureLimitEnforcer.canCreateAgent(tenantId)` with 5 agents already
3. Returns false

### TC-TR-004: Trial → Paid Conversion
1. Activate trial
2. `POST /tenants/register?plan=PRO` (or dedicated convert endpoint)
3. Verify plan=PRO, maxAgents=50, maxUsers=100

## Structured Logging Tests

### TC-LOG-001: MDC Fields Set
1. Send request with `X-Correlation-Id: test-123`
2. Verify log entry contains `correlationId: "test-123"`

### TC-LOG-002: Auto-generated Correlation ID
1. Send request without correlation ID header
2. Verify log entry contains auto-generated UUID

### TC-LOG-003: JSON Log Format
1. Verify log output is valid JSON
2. Verify fields: @timestamp, level, service, message

## Integration Tests

### TC-INT-001: Full Auth Flow
1. Register tenant
2. Login with valid credentials (no MFA)
3. Refresh token → new pair
4. Use old refresh → expect 401 (reuse detection)
5. Login again (family revoked) → expect 401 (theft detected)

### TC-INT-002: Full MFA Flow
1. Login → MFA not required
2. Setup MFA
3. Verify MFA
4. Logout
5. Login → MFA challenge
6. Valid code → tokens
7. Invalid code → error
