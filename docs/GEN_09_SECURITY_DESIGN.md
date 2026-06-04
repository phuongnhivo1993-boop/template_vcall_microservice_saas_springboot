# Security Design (v2)

## 1. Authentication

### Password-based Login
- JWT with RS256: Access token (15 min) + Refresh token (7 day)
- Refresh rotation + theft detection: One-time-use, family tracking
- Password policy: 8-100 chars, uppercase, lowercase, digit, special
- Account lockout: 5 failed attempts → 30-min lockout
- Rate limiting: 5 req/min on `/auth/login` per IP

### Multi-Factor Authentication (TOTP)
- **Enrollment**: `POST /mfa/setup` → QR code → `POST /mfa/verify`
- **Verification**: 6-digit code, 30s window, 1-step drift tolerance
- **Login Flow**: If `mfaEnabled`, return `mfaToken` → challenge → verify → tokens
- **Secrets**: Encrypted via AES-256-GCM before DB storage

### OAuth2/SSO (Planned)
- Google, Facebook social login (Spring Security OAuth2 client)

## 2. Authorization
- **RBAC**: ADMIN, SUPERVISOR, AGENT roles
- **tenant_id isolation**: All queries filtered by tenant_id
- **Per-tenant feature limits**: FeatureLimitEnforcer checks plan tier before operations

## 3. Data Security
- **PII Tagging**: `@PII` annotation on email, phone, full_name
- **Encryption at Rest**: AES-256-GCM via EncryptionUtil
- **Password Hashing**: BCrypt (strength 12)
- **Token Hashing**: SHA-256 for refresh tokens in DB
- **MFA Secret**: AES-256-GCM encrypted

## 4. Network Security
- **TLS**: All external + inter-service communication
- **API Gateway**: Single entry point, rate limited
- **Per-tenant Rate Limiting**: Plan-based (50-2000 req/min)
- **CORS**: Whitelist origins only
- **Content Security Policy**: Via gateway filter

## 5. Audit
- **Auditable entities**: @CreatedDate, @LastModifiedDate, @CreatedBy
- **Login Audit**: LoginAttempts table (IP, user-agent, success/failure)
- **Admin audit**: User management, config changes logged

## 6. Secrets Management
- **No hard-coded secrets**: Externalized via Spring Cloud Config / environment
- **Encryption key**: `security.encryption.secret` (production: Vault)
- **API key storage**: Encrypted at rest via EncryptionUtil

## 7. HIPAA/GDPR Controls
| Control | Implementation |
|---------|---------------|
| PII Identification | @PII annotation |
| Access Control | RBAC per tenant |
| Encryption at Rest | AES-256-GCM for PII + MFA secrets |
| Right to Access | GET /gdpr/export |
| Right to Erasure | DELETE /gdpr/delete |
| Audit Trail | LoginAttempts + @Auditable |
| Data Retention | Legal retention after anonymization |
