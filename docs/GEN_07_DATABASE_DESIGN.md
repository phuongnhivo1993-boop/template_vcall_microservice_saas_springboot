# Database Design (v2)

## Key Entities & Changes

### `users` (iam-service) — MFA fields added
| Column | Type | Constraints |
|--------|------|-------------|
| id | UUID | PK |
| tenant_id | VARCHAR(50) | FK, NOT NULL |
| username | VARCHAR(100) | UNIQUE(tenant_id) |
| password | VARCHAR(255) | BCrypt hash |
| email | VARCHAR(255) | @PII |
| phone | VARCHAR(20) | @PII |
| full_name | VARCHAR(255) | @PII |
| role | VARCHAR(20) | ADMIN/SUPERVISOR/AGENT |
| status | VARCHAR(20) | ACTIVE/INACTIVE/SUSPENDED |
| password_changed_at | TIMESTAMP | |
| **mfa_enabled** | **BOOLEAN** | **DEFAULT false** |
| **mfa_secret** | **VARCHAR(255)** | **Encrypted TOTP secret** |
| **failed_attempts** | **INT** | **DEFAULT 0** |
| **locked_until** | **TIMESTAMP** | |
| created_at | TIMESTAMP | |
| updated_at | TIMESTAMP | |

### `refresh_tokens` (iam-service)
| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGINT | PK AUTO |
| user_id | UUID | FK → users |
| token_hash | VARCHAR(255) | UNIQUE, SHA-256 |
| family | VARCHAR(50) | Rotation family |
| expires_at | TIMESTAMP | |
| revoked | BOOLEAN | DEFAULT false |

### `login_attempts` (iam-service)
| Column | Type | Constraints |
|--------|------|-------------|
| id | BIGINT | PK AUTO |
| username | VARCHAR(100) | |
| ip_address | VARCHAR(45) | |
| user_agent | VARCHAR(500) | |
| success | BOOLEAN | |
| attempted_at | TIMESTAMP | |

### `tenants`
| Column | Type | Constraints |
|--------|------|-------------|
| id | VARCHAR(50) | PK |
| company_name | VARCHAR(200) | UNIQUE |
| domain | VARCHAR(255) | UNIQUE |
| plan | VARCHAR(20) | TRIAL/BASIC/PRO/ENTERPRISE |
| status | VARCHAR(20) | ACTIVE/SUSPENDED/CANCELED |
| trial_ends_at | TIMESTAMP | |
| config | JSONB | Tenant settings |
| created_at | TIMESTAMP | |
| updated_at | TIMESTAMP | |

## Multi-tenancy
- Shared database per service, isolated by `tenant_id`
- Auto-set via `@PrePersist` on `BaseEntity`
- Filtered via `TenantFilter` (header → JWT → parameter)

## Migration Strategy (Flyway)
| Migration | Contents |
|-----------|----------|
| V1__initial | Base service tables |
| V2__refresh_tokens_and_tenant | Refresh tokens, login_attempts, tenant_id columns |
| V3__password_policy | password_changed_at, locked_until, failed_attempts |
| V4__mfa | mfa_enabled, mfa_secret columns on users |

## Indexes
- `users(tenant_id, username)` UNIQUE
- `users(tenant_id, email)` UNIQUE
- `refresh_tokens(user_id, family)`
- `refresh_tokens(family, revoked)`
- `login_attempts(username, attempted_at)`
