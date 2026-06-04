# Business Requirements (v2)

## BR-001: Multi-tenant SaaS Platform
- tenant_id isolation on all entities
- Per-tenant rate limiting and feature limits
- Tenant provisioning < 5 min
- Trial lifecycle: 14-day trial, auto-expiry notification, conversion to paid

## BR-002: Security & Compliance
- TOTP MFA for all admin accounts
- Refresh token rotation with family tracking and theft detection
- Account lockout after 5 failed attempts
- Password policy (8-100 chars, uppercase, lowercase, digit, special)
- PII tagging, GDPR export/erasure, audit trail

## BR-003: Omnichannel Communication
- Voice (SIP/PBX/IVR), Chat, Email, SMS from unified interface
- Real-time agent status via WebSocket
- Channel routing rules and SLA per channel

## BR-004: Observability
- Structured JSON logging (Logstash schema)
- Distributed tracing (OpenTelemetry → Jaeger)
- Prometheus metrics per service
- Dead letter queue for Kafka failures
- Custom health indicators per service

## BR-005: Agent Productivity
- Real-time status updates (<1s)
- Customer 360 view
- Ticket SLA monitoring with escalation
- Quick access to past interactions

## BR-006: Scalability & Reliability
- Circuit breakers, retry with exponential backoff, bulkhead (10 concurrent)
- Stateless services for horizontal scaling
- Rolling deployment (zero-downtime)
- Multi-AZ with RPO 1hr, RTO 4hr
