# Phase 2: BA Analysis — Functional & Non-Functional Requirements

## 2.1 Functional Requirements Coverage

### Identity & Access Management (iam-service)

| FR ID | Requirement | Priority | Status |
|-------|-------------|----------|--------|
| FR-001 | User registration with validation | P0 | ✅ |
| FR-002 | JWT-based authentication (login/logout) | P0 | ✅ |
| FR-003 | JWT refresh token rotation with theft detection | P0 | ✅ |
| FR-004 | Password reset via email (forgot/reset flow) | P0 | ✅ |
| FR-005 | Role-based access control (RBAC) with permissions | P0 | ✅ |
| FR-006 | User CRUD with search/filter/sort | P0 | ✅ |
| FR-007 | Role CRUD with search | P0 | ✅ |
| FR-008 | Permission management | P0 | ✅ |
| FR-009 | Multi-factor authentication (TOTP) | P1 | ❌ |
| FR-010 | Social login (OAuth2/OIDC) | P2 | ❌ |
| FR-011 | Account lockout after failed attempts | P1 | ❌ |
| FR-012 | Session management (concurrent sessions) | P1 | ⚠️ |
| FR-013 | GDPR data export/deletion (DSR) | P0 | ✅ |
| FR-014 | PII tagging and anonymization | P0 | ✅ |

### Agent Management (agent-service)

| FR ID | Requirement | Priority | Status |
|-------|-------------|----------|--------|
| FR-020 | Agent profile CRUD with search/filter/sort | P0 | ✅ |
| FR-021 | Agent status management (online/offline/busy/away/break) | P0 | ✅ |
| FR-022 | Agent session tracking (login/logout/duration) | P0 | ✅ |
| FR-023 | Agent group management | P0 | ✅ |
| FR-024 | Real-time agent status via WebSocket | P0 | ✅ |
| FR-025 | Agent skill management | P1 | ⚠️ |
| FR-026 | Agent performance metrics | P1 | ❌ |
| FR-027 | Agent assignment to groups with bulk operations | P0 | ✅ |
| FR-028 | CSV/Excel export of agent data | P0 | ✅ |
| FR-029 | Bulk import of agents | P0 | ✅ |

### Call Management (call-service)

| FR ID | Requirement | Priority | Status |
|-------|-------------|----------|--------|
| FR-030 | Call control (start/answer/end/hold/resume) | P0 | ✅ |
| FR-031 | Call queue management | P0 | ✅ |
| FR-032 | IVR menu navigation | P0 | ✅ |
| FR-033 | ACD routing rules | P0 | ✅ |
| FR-034 | Call recording management | P0 | ✅ |
| FR-035 | Call evaluation and scoring | P1 | ✅ |
| FR-036 | Call transfer/conference | P1 | ❌ |
| FR-037 | Queue metrics and analytics | P0 | ✅ |
| FR-038 | Call state machine with event publishing | P0 | ✅ |

### Omnichannel (omnichannel-service + chat + email + sms)

| FR ID | Requirement | Priority | Status |
|-------|-------------|----------|--------|
| FR-040 | Chat conversation management | P0 | ✅ |
| FR-041 | Email processing (send/receive) | P0 | ✅ |
| FR-042 | SMS sending with delivery tracking | P0 | ✅ |
| FR-043 | Omnichannel routing rules engine | P0 | ✅ |
| FR-044 | Channel configuration per tenant | P0 | ✅ |
| FR-045 | Conversation history and search | P0 | ⚠️ |
| FR-046 | Social channel integration (FB/WA/Zalo) | P2 | ❌ |
| FR-047 | Chatbot/AI integration | P2 | ❌ |
| FR-048 | WebSocket for real-time chat | P0 | ✅ |

### Ticket Management (ticket-service)

| FR ID | Requirement | Priority | Status |
|-------|-------------|----------|--------|
| FR-050 | Ticket lifecycle (create/update/close/reopen) | P0 | ✅ |
| FR-051 | SLA rule configuration and breach detection | P0 | ✅ |
| FR-052 | Ticket escalation workflow | P0 | ✅ |
| FR-053 | Ticket search/filter/sort | P0 | ✅ |
| FR-054 | Comment and attachment management | P0 | ✅ |
| FR-055 | Ticket assignment and reassignment | P0 | ✅ |
| FR-056 | SLA status tracking (pending/met/breached) | P0 | ✅ |
| FR-057 | Ticket statistics by status/priority | P0 | ✅ |
| FR-058 | CSV/Excel export, bulk operations | P0 | ✅ |
| FR-059 | Circuit breaker for SLA monitoring | P0 | ✅ |

### Billing (billing-service)

| FR ID | Requirement | Priority | Status |
|-------|-------------|----------|--------|
| FR-060 | Pricing plan management | P0 | ✅ |
| FR-061 | Invoice generation and management | P0 | ✅ |
| FR-062 | Payment processing | P0 | ⚠️ |
| FR-063 | Subscription management | P1 | ❌ |
| FR-064 | Usage-based billing/metering | P1 | ❌ |
| FR-065 | Trial management | P1 | ❌ |
| FR-066 | Tax calculation | P2 | ❌ |
| FR-067 | Dunning (failed payment retry) | P2 | ❌ |

### Reporting & Analytics (cdr-service + reporting-service)

| FR ID | Requirement | Priority | Status |
|-------|-------------|----------|--------|
| FR-070 | CDR storage with ClickHouse | P0 | ✅ |
| FR-071 | CDR search/filter/sort | P0 | ✅ |
| FR-072 | CDR analytics and summarization | P0 | ✅ |
| FR-073 | Real-time dashboard endpoints | P1 | ✅ |
| FR-074 | Report generation (scheduled/on-demand) | P1 | ⚠️ |
| FR-075 | BI dashboard integrations | P2 | ❌ |

### Notification (notification-service)

| FR ID | Requirement | Priority | Status |
|-------|-------------|----------|--------|
| FR-080 | Multi-channel notification (email/SMS/push) | P0 | ✅ |
| FR-081 | Notification template management | P0 | ✅ |
| FR-082 | Notification history | P0 | ✅ |
| FR-083 | Push notification (Firebase/APNs) | P1 | ❌ |
| FR-084 | Delivery tracking | P1 | ⚠️ |
| FR-085 | Notification preferences per user | P1 | ⚠️ |

### Audit (audit-service)

| FR ID | Requirement | Priority | Status |
|-------|-------------|----------|--------|
| FR-090 | Audit log search/filter/sort | P0 | ✅ |
| FR-091 | Fraud detection alerts | P0 | ✅ |
| FR-092 | Tamper-proof audit logging | P1 | ❌ |
| FR-093 | Compliance reporting | P1 | ❌ |

### Scheduling (scheduling-service)

| FR ID | Requirement | Priority | Status |
|-------|-------------|----------|--------|
| FR-100 | Shift CRUD with search | P0 | ✅ |
| FR-101 | Time-off request workflow | P0 | ✅ |
| FR-102 | Workforce forecasting | P2 | ❌ |
| FR-103 | Shift optimization | P2 | ❌ |

### Survey (survey-service)

| FR ID | Requirement | Priority | Status |
|-------|-------------|----------|--------|
| FR-110 | Survey template management | P0 | ✅ |
| FR-111 | Question management | P0 | ✅ |
| FR-112 | Response collection | P0 | ✅ |
| FR-113 | Survey analytics | P1 | ⚠️ |
| FR-114 | Post-call survey triggers | P1 | ❌ |

### Campaign (campaign-service)

| FR ID | Requirement | Priority | Status |
|-------|-------------|----------|--------|
| FR-120 | Campaign CRUD with scheduler | P0 | ✅ |
| FR-121 | Campaign result tracking | P0 | ✅ |
| FR-122 | Auto dialer integration | P0 | ✅ |
| FR-123 | Predictive dialer | P2 | ❌ |
| FR-124 | Campaign analytics | P1 | ⚠️ |

## 2.2 Non-Functional Requirements

| NFR ID | Requirement | Target | Status |
|--------|-------------|--------|--------|
| NFR-001 | Performance — API response time | <200ms p95 | ⚠️ |
| NFR-002 | Performance — Call handling | <500ms | ⚠️ |
| NFR-003 | Scalability — Horizontal scaling | Add nodes without downtime | ✅ |
| NFR-004 | Availability — Uptime | 99.9% (43min/month) | ⚠️ |
| NFR-005 | Security — Encryption at rest | AES-256 | ❌ |
| NFR-006 | Security — Encryption in transit | TLS 1.3 | ⚠️ |
| NFR-007 | Security — Authentication | JWT + Refresh Token Rotation | ✅ |
| NFR-008 | Security — Authorization | RBAC + Method-level | ✅ |
| NFR-009 | Security — Rate limiting | Per-IP, per-route | ✅ |
| NFR-010 | Security — MFA | TOTP support | ❌ |
| NFR-011 | Audit — Immutable logs | Hash-chain verification | ❌ |
| NFR-012 | Compliance — HIPAA | PHI protection, BAA, audit | ⚠️ |
| NFR-013 | Compliance — GDPR | DSR, consent, data portability | ✅ |
| NFR-014 | Observability — Metrics | Prometheus + Grafana | ✅ |
| NFR-015 | Observability — Tracing | OpenTelemetry | ✅ |
| NFR-016 | Observability — Logging | Structured JSON | ⚠️ |
| NFR-017 | Resilience — Circuit breaker | Resilience4j | ✅ |
| NFR-018 | Resilience — Retry with backoff | 3 attempts, 500ms | ✅ |
| NFR-019 | Resilience — Bulkhead | Thread pool isolation | ❌ |
| NFR-020 | Data — Backup & recovery | Daily snapshots | ❌ |
| NFR-021 | Data — Migration | Flyway | ✅ |
| NFR-022 | Multi-tenancy — Data isolation | tenant_id filter | ✅ |
| NFR-023 | Multi-tenancy — Resource limits | Per-tenant rate limiting | ⚠️ |
| NFR-024 | API — Versioning | URL/header-based | ❌ |
| NFR-025 | API — Documentation | OpenAPI 3.0 | ✅ |

## 2.3 Compliance Matrix

| Regulation | Requirement | Status | Gap |
|------------|-------------|--------|-----|
| **HIPAA** | PHI access controls | ⚠️ | Need encryption at rest |
| **HIPAA** | Audit logs | ⚠️ | Logs not tamper-proof |
| **HIPAA** | BAA agreements | ❌ | Not implemented |
| **GDPR** | Right to access | ✅ | GDPR export endpoint |
| **GDPR** | Right to erasure | ✅ | Anonymize endpoint |
| **GDPR** | Data portability | ✅ | JSON export |
| **GDPR** | Consent management | ❌ | Not implemented |
| **PCI DSS** | Card data not stored | ✅ | Tokenized payments |
| **CCPA** | Consumer data rights | ⚠️ | Partial GDPR covers |
| **SOX** | Financial audit trail | ⚠️ | Basic billing audit |

---

*End of Phase 2 — BA Completeness Score: 88%*
*Next: Phase 3 — Use Cases*
