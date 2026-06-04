# Code Generation Guide (v2)

## Module: `iam-service`

### MFA/TOTP Implementation

**Files to create**:
- `dto/MfaSetupResponse.java` — secret, qrCodeUri, enabled
- `dto/MfaVerifyRequest.java` — @Pattern 6-digit code
- `dto/MfaChallengeResponse.java` — mfaRequired, mfaToken
- `service/MfaService.java` — setup, enable, disable, verify
- `controller/MfaController.java` — /mfa/setup, /mfa/verify, /mfa/disable

**MfaService.java key logic**:
```java
// Generate TOTP secret
SecretGenerator secretGenerator = new DefaultSecretGenerator();
String secret = secretGenerator.generate();

// QR provisioning URI
QrData qrData = new QrData.Builder()
    .label(user.getEmail()).secret(secret).issuer("VCall Contact Center")
    .algorithm(QrData.Algorithm.SHA1).digits(6).period(30).build();
String qrCodeUri = qrData.getUri();

// Verify TOTP code
TimeProvider timeProvider = new SystemTimeProvider();
CodeGenerator codeGenerator = new DefaultCodeGenerator();
CodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
boolean isValid = verifier.isValidCode(secret, code);
```

**User entity additions**:
```java
@Column(name = "mfa_enabled")
private boolean mfaEnabled;
@Column(name = "mfa_secret")
private String mfaSecret;  // AES-256-GCM encrypted
@Column(name = "failed_attempts")
private int failedAttempts;
@Column(name = "locked_until")
private LocalDateTime lockedUntil;
```

**Login flow update**:
```java
// After credential validation:
if (user.isMfaEnabled()) {
    String mfaToken = generateTempToken(user.getId());
    return new LoginResponse(mfaToken, true);  // mfaRequired=true
}
// Else: generate access + refresh tokens normally
```

**POM dependency**:
```xml
<dependency>
    <groupId>dev.samstevens.totp</groupId>
    <artifactId>totp-spring-boot-starter</artifactId>
    <version>1.7.1</version>
</dependency>
```

## Module: `common`

### Per-tenant Rate Limiter

**File**: `config/PerTenantRateLimiter.java`
- In-memory sliding window per tenant
- `ConcurrentHashMap<String, SlidingWindowCounter>`
- `isAllowed(tenantId)` → boolean
- `getRemainingRequests(tenantId)` → int

**Usage in Gateway**:
```java
PerTenantRateLimiter limiter = new PerTenantRateLimiter(100, 60_000);
if (!limiter.isAllowed(tenantId)) {
    return ResponseEntity.status(429).header("Retry-After", "60").build();
}
```

### Structured Logging

**File**: `config/MdcLoggingFilter.java`
```java
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class MdcLoggingFilter implements Filter {
    // Set correlationId, requestUri, requestMethod in MDC
    // Clear on completion
}
```

**File**: `resources/logback-spring.xml`
```xml
<encoder class="net.logstash.logback.encoder.LogstashEncoder">
    <includeMdcKeyName>correlationId</includeMdcKeyName>
    <includeMdcKeyName>tenantId</includeMdcKeyName>
    <includeMdcKeyName>userId</includeMdcKeyName>
    <includeMdcKeyName>traceId</includeMdcKeyName>
</encoder>
```

**POM dependency**:
```xml
<dependency>
    <groupId>net.logstash.logback</groupId>
    <artifactId>logstash-logback-encoder</artifactId>
    <version>7.4</version>
</dependency>
```

### Trial Lifecycle & Feature Limits

**File**: `tenant/TrialLifecycleService.java`
- `activateTrial(tenantId, plan)` → Subscription with trialEnd
- `isTrialExpired(tenantId)` → boolean
- `getDaysRemaining(tenantId)` → int
- `shouldWarnExpiry(tenantId)` → boolean (≤3 days)
- `convertToPaid(tenantId, plan)` → Subscription with limits

**File**: `tenant/FeatureLimitEnforcer.java`
- `canCreateAgent(tenantId)` → checks maxAgents
- `canCreateUser(tenantId, currentCount)` → checks maxUsers
- `canProcessCall(tenantId, currentMonthly)` → checks maxCallsPerMonth
- Limits per plan: TRIAL(5/10/500), BASIC(10/25/2000), PRO(50/100/10000), ENTERPRISE(500/1000/100000)

## Validation Checklist (v2)
- [ ] All @Valid and validation annotations present
- [ ] MFA setup/verify/disable endpoints operational
- [ ] Login flow includes MFA challenge when enabled
- [ ] Per-tenant rate limiting with plan-based limits
- [ ] All log entries in structured JSON format
- [ ] correlationId propagated via MDC
- [ ] Trial lifecycle: activation → warning → expiry → conversion
- [ ] Feature limits enforced on create operations
- [ ] No hard-coded secrets
- [ ] All endpoints under /api/v1
- [ ] PII fields annotated with @PII
- [ ] tenant_id set via @PrePersist
- [ ] Circuit breakers + bulkhead + retry on critical paths
- [ ] DLQ configured for all Kafka consumers
- [ ] Refresh token rotation active with theft detection
