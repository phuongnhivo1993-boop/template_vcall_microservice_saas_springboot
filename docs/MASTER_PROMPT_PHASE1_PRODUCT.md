# Phase 1: Product Analysis — VCall Contact Center

## 1.1 Product Canvas

| Element | Description |
|---------|-------------|
| **Product Name** | VCall Contact Center |
| **Vision** | Omnichannel Healthcare Contact Center SaaS platform for enterprises like Medihub, hospitals, clinics, and large-scale customer service centers |
| **Target Users** | Healthcare providers, contact center agents, supervisors, administrators, customers/patients |
| **Core Problem** | Fragmented customer communication across channels; need for unified, compliant, intelligent healthcare contact center |
| **Key Differentiator** | Purpose-built for healthcare with omnichannel support + voice platform (Kamailio/FreeSWITCH) + SaaS multi-tenancy |
| **Revenue Model** | SaaS subscription with tiered pricing (per-agent, per-channel, per-feature) |
| **Distribution** | Web (Next.js 14), Mobile (React Native + Expo), API-first, Voice (SIP/WebRTC) |

## 1.2 Business Goals

| Goal | Metric | Priority |
|------|--------|----------|
| Unified customer communication | % of interactions handled in-platform | P0 |
| Healthcare compliance | HIPAA/GDPR audit pass rate | P0 |
| Agent productivity | Avg handling time reduction | P1 |
| Customer satisfaction | CSAT score improvement | P1 |
| Scalable SaaS | Tenant onboarding time < 15min | P0 |
| Revenue growth | Monthly recurring revenue (MRR) | P1 |

## 1.3 Stakeholder Map

| Stakeholder | Primary Needs | Current Support | Gap |
|-------------|---------------|-----------------|-----|
| **Customer/Patient** | Reach support via any channel, quick resolution, self-service | Partial (no customer portal, no self-service IVR) | Customer self-service portal, knowledge base |
| **Agent** | Unified agent desktop, skill-based routing, performance feedback | Partial (services exist, no real-time desktop) | Unified agent desktop UI, skill management |
| **Supervisor** | Real-time monitoring, quality management, reporting | Weak (reporting-service exists but basic) | Real-time dashboard, QA workflow |
| **Administrator** | User/role management, tenant config, billing | Partial (iam-service good, multi-tenant implicit) | Tenant onboarding, subscription management |
| **Compliance Officer** | Audit trail, data retention, GDPR/HIPAA | Weak (audit-service exists) | Compliance framework, DSR APIs |
| **Developer** | Clean APIs, documentation, event contracts | Moderate (OpenAPI + Kafka events) | AsyncAPI docs, schema registry |

## 1.4 User Personas

### Persona 1: Agent — Nguyen Van A
- **Role**: Contact center agent
- **Needs**: Handle calls/chats/emails from one interface, quick access to customer info, follow scripts
- **Pain points**: Switching between systems, manual data entry, no customer history
- **Goals**: Resolve customer issues efficiently, meet SLA targets

### Persona 2: Supervisor — Tran Thi B
- **Role**: Team supervisor
- **Needs**: Monitor agent status in real-time, listen to calls, view reports
- **Pain points**: No real-time visibility, manual quality checks
- **Goals**: Improve team performance, ensure quality standards

### Persona 3: Administrator — Pham Van C
- **Role**: System admin
- **Needs**: Manage users/roles, configure tenants, billing, monitor system health
- **Pain points**: Manual tenant setup, no usage analytics
- **Goals**: Keep system running, onboard new tenants quickly

### Persona 4: Customer — Le Thi D
- **Role**: Patient/end customer
- **Needs**: Reach support quickly via any channel, self-service options
- **Pain points**: Long wait times, repeating information
- **Goals**: Get issues resolved fast without hassle

### Persona 5: Compliance Officer — Hoang Van E
- **Role**: Healthcare compliance manager
- **Needs**: Audit logs, data retention, consent management, breach notifications
- **Pain points**: No HIPAA/GDPR controls, manual audit preparation
- **Goals**: Pass regulatory audits, protect patient data

## 1.5 Value Proposition

| Theme | Delivered | Gap |
|-------|-----------|-----|
| **Unified Omnichannel** | Chat, Email, SMS services + routing | Missing social channels (Facebook, WhatsApp, Zalo, Telegram) |
| **Voice Platform** | SIP, PBX, call control, recording | No IVR designer, no speech analytics |
| **Agent Productivity** | Agent CRUD, session tracking | No unified agent desktop, no skill management |
| **Customer 360** | customer-service + customer360-service | No interaction timeline, no predictive insights |
| **Compliance & Audit** | audit-service, recording-service | No HIPAA/GDPR-specific controls |
| **Scalable SaaS** | microservices, Kafka, polyglot persistence | Multi-tenancy not explicit in data model |

## 1.6 Competitive Landscape

| Feature | VCall | Competitor A | Competitor B |
|---------|-------|-------------|-------------|
| Omnichannel | ✅ Chat/Email/SMS | ✅ All channels | ✅ All channels |
| Voice Platform | ✅ Full SIP/PBX | ✅ | ✅ |
| Healthcare Focus | ✅ Purpose-built | ❌ General | ❌ General |
| SaaS Multi-tenancy | ⚠️ Partial | ✅ | ✅ |
| AI/ML Integration | ❌ Planned | ✅ | ✅ |
| Mobile App | ✅ React Native | ✅ | ❌ |
| Open API | ✅ OpenAPI | ⚠️ Limited | ✅ |
| HIPAA Compliance | ⚠️ Partial | ✅ | ❌ |

## 1.7 Feature Priority Matrix

| Feature | Business Value | Effort | Priority |
|---------|---------------|--------|----------|
| Multi-tenancy (tenant_id isolation) | Critical | High | P0 |
| JWT Refresh Token Rotation | High | Medium | P0 |
| Rate Limiting | High | Low | P0 |
| GDPR/PII Compliance | Critical | High | P0 |
| Password Reset Flow | High | Low | P1 |
| WebSocket Real-time Agent Status | High | Medium | P1 |
| Circuit Breaker/Resilience4j | High | Medium | P1 |
| Search/Filter/Sort (all services) | Medium | High | P1 |
| Distributed Tracing | Medium | Medium | P1 |
| Flyway DB Migrations | Medium | Medium | P1 |
| Social Channel Integration | High | Very High | P2 |
| Knowledge Base | High | High | P2 |
| AI Chatbot | High | High | P2 |
| IVR Designer | Medium | High | P2 |

---

*End of Phase 1 — Product Completeness Score: 85%*
*Next: Phase 2 — BA Analysis*
