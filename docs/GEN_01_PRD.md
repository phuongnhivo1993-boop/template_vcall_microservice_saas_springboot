# Product Requirements Document — VCall Contact Center (v2)

## 1. Executive Summary
VCall Contact Center is an omnichannel healthcare contact center SaaS platform. Enables healthcare providers to manage communications across voice, chat, email, SMS from a unified platform with HIPAA/GDPR compliance, multi-tenant SaaS, MFA security, and real-time agent collaboration.

## 2. Product Vision
Leading omnichannel healthcare contact center platform in Vietnam and SEA — enabling healthcare enterprises to deliver exceptional patient experiences through intelligent, compliant, multi-channel communication.

## 3. Target Market
Healthcare providers, large customer service centers, BPO contact centers

## 4. Core Features (P0/P1)
| Feature | Priority | Status |
|---------|----------|--------|
| Omnichannel Inbox | P0 | Chat + Email + SMS + Voice |
| Voice Platform (SIP/IVR/ACD) | P0 | PBX, recording, IVR steps |
| Ticket Management | P0 | SLA, escalation, lifecycle |
| Agent Desktop | P1 | Real-time WebSocket status, customer 360 |
| Reporting & Dashboards | P1 | CDR analytics, team KPIs |
| SaaS Multi-tenancy | P0 | tenant_id, per-tenant limits, trial lifecycle |
| Compliance (GDPR/HIPAA) | P0 | PII tagging, export, erasure, audit |
| Security (MFA/JWT/Refresh) | P0 | TOTP MFA, refresh rotation, lockout |
| Mobile App | P1 | React Native agent app + customer portal |
| Knowledge Base | P1 | Article management, search |
| Campaign Management | P1 | Outbound predictive dialer |

## 5. Key Improvements (v2)
- TOTP MFA for admin accounts
- Per-tenant rate limiting and feature limits
- Trial lifecycle management (14-day, auto-expiry, conversion)
- Structured JSON logging with MDC (correlationId, tenantId, traceId)
- Dead letter queue with exponential backoff for Kafka
- API versioning (/api/v1 prefix)
- Bulkhead isolation (10 concurrent calls)
- Expanded UI/UX spec (30+ screens, all states)

## 6. Release Criteria
- All P0 features implemented and tested
- API response time <200ms p95
- tenant_id isolation verified
- Refresh token rotation + theft detection active
- MFA setup/verify/disable endpoints operational
- Per-tenant rate limiting active
- Circuit breakers + bulkhead on critical paths
- Trial lifecycle: activation → expiry → conversion
- GDP DSR endpoints operational

## 7. Success Metrics
| Metric | Target |
|--------|--------|
| API Availability | 99.9% |
| Auth Response Time | <100ms |
| MFA Verify Response | <200ms |
| Tenant Onboarding | <5 min |
| Trial → Paid Conversion | >20% |
| CSAT Score | >4.5/5 |
