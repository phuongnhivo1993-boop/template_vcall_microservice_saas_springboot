# Phase 3: Use Cases

## 3.1 Actor Catalog

| Actor | Description | System Representation |
|-------|-------------|----------------------|
| **Customer** | End-user contacting support via any channel | Customer entity (customer-service) |
| **Agent** | Support agent handling interactions | Agent entity (agent-service) |
| **Supervisor** | Monitors agents and quality | Role: SUPERVISOR (iam-service) |
| **Admin** | System configuration and management | Role: SUPER_ADMIN (iam-service) |
| **System** | Automated processes and integrations | Kafka events, scheduled jobs |
| **Compliance Officer** | Ensures regulatory compliance | Role: QA/SUPER_ADMIN |
| **Guest** | Unauthenticated user (password reset, etc.) | No entity |

## 3.2 Primary Use Cases

### UC-01: User Login
| Flow | Description |
|------|-------------|
| **Main Flow** | 1. User provides username + password → 2. System validates credentials → 3. System generates access token + refresh token (with family) → 4. System returns tokens + user info |
| **Alternative Flow** | Invalid credentials → Return 401 with error message |
| **Exception Flow** | Account locked/suspended → Return 403 with reason |
| **Validation** | Username not empty, password not empty, email format for password reset |
| **Business Rules** | Max 5 failed attempts before lockout (TBD), refresh token valid for 7 days |

### UC-02: Refresh Token
| Flow | Description |
|------|-------------|
| **Main Flow** | 1. User provides refresh token → 2. System validates token signature & expiry → 3. System marks old token revoked → 4. System generates new token pair (same family) → 5. Returns new tokens |
| **Alternative Flow** | Token expired → Return 401, client must re-login |
| **Exception Flow** | Token reuse detected (revoked token presented) → Revoke entire family → Return 401 with security alert |
| **Business Rules** | Each refresh token can only be used once (rotation), token family tracks lineage |

### UC-03: Password Reset
| Flow | Description |
|------|-------------|
| **Main Flow** | 1. User requests reset with email → 2. System generates 30-min token → 3. Returns token (in dev) or sends via email → 4. User submits token + new password → 5. System validates token → 6. Updates password |
| **Alternative Flow** | Invalid/expired token → Return 400 with error |
| **Business Rules** | Token expires in 30 minutes, one-time use, new password must meet complexity rules |

### UC-04: Handle Inbound Call
| Flow | Description |
|------|-------------|
| **Main Flow** | 1. Call arrives via SIP → 2. System identifies tenant → 3. IVR menu plays → 4. Customer selects option → 5. ACD routes to available agent → 6. Agent answers → 7. Recording starts → 8. Call events published to Kafka |
| **Alternative Flow** | No agent available → Call goes to queue with position announcement |
| **Alternative Flow** | Queue full → Play busy tone or route to voicemail |
| **Business Rules** | Max queue time 5min, overflow to backup group if no answer |

### UC-05: Omnichannel Conversation
| Flow | Description |
|------|-------------|
| **Main Flow** | 1. Customer sends message (chat/email/SMS) → 2. Service receives → 3. Omnichannel routing engine evaluates rules → 4. Conversation created/continued → 5. Agent assigned based on skills → 6. Message delivered → 7. Events published |
| **Alternative Flow** | No matching routing rule → Default queue assignment |
| **Business Rules** | Same customer across channels = same conversation | 

### UC-06: Ticket Lifecycle
| Flow | Description |
|------|-------------|
| **Main Flow** | 1. Ticket created (auto from call/chat or manual) → 2. SLA timer starts → 3. Agent works ticket → 4. Status changes tracked with history → 5. Ticket resolved/closed → 6. Survey triggered |
| **Alternative Flow** | SLA breach → Escalation notification sent → Supervisor notified |
| **Exception Flow** | Ticket cannot be deleted (soft delete only) |
| **Business Rules** | Status flow: OPEN → IN_PROGRESS → RESOLVED → CLOSED, can reopen from CLOSED |

### UC-07: Agent Real-time Status
| Flow | Description |
|------|-------------|
| **Main Flow** | 1. Agent logs in (session starts) → 2. Status set to ONLINE → 3. WebSocket broadcasts to /topic/agent-status → 4. Supervisors see real-time update → 5. Agent changes status → 6. Broadcast again |
| **Business Rules** | Status transitions: OFFLINE → ONLINE (login), ONLINE → BUSY (on call), BUSY → ONLINE (call end), any → BREAK/AWAY (manual) |

### UC-08: GDPR Data Subject Request
| Flow | Description |
|------|-------------|
| **Main Flow** | 1. User requests data export → 2. System collects PII-tagged fields → 3. Exports as JSON → 4. User can download |
| **Alternative Flow** | User requests deletion → System anonymizes PII fields, marks account as deleted |
| **Business Rules** | DSR must be fulfilled within 30 days (GDPR), some data retained for legal/compliance |

## 3.3 Missing Critical Use Cases

| UC ID | Name | Rationale | Priority |
|-------|------|-----------|----------|
| UC-20 | Customer Self-Registration | No customer-facing signup | P1 |
| UC-21 | Call Transfer/Conference | Missing call control feature | P1 |
| UC-22 | Social Channel Integration | FB, WhatsApp, Zalo adapters missing | P2 |
| UC-23 | Knowledge Base Search | No KB service for agent/customer | P2 |
| UC-24 | Quality Call Scoring | QA evaluation workflow | P2 |
| UC-25 | Tenant Onboarding | No tenant provisioning flow | P1 |
| UC-26 | Subscription Management | No plan change/upgrade/downgrade | P1 |
| UC-27 | MFA/TOTP Setup | No multi-factor authentication | P1 |

---

*End of Phase 3 — Use Case Completeness: 85%*
*Next: Phase 4 — CRUD Gap Analysis*
