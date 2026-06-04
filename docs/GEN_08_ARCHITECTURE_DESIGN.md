# Architecture Design (v2)

## Updated Architecture Diagram
```
                    Client Layer (Next.js / React Native / SIP Phone)
                               │ HTTPS / WSS
                    ┌──────────▼──────────┐
                    │   API Gateway (8080)  │
                    │ Rate Limiter (global + per-tenant) │
                    │ MdcLoggingFilter                              │
                    │ JWT Auth │ Tenant Filter                      │
                    │ Spring Cloud Gateway                          │
                    └──┬──┬──┬──┬──┬──┬──┬──┘
   ┌───────────────────┼──┼──┼──┼──┼──┼──┼──────────────────────┐
   │  IAM (8081) │Ticket(8082) │Chat(8083) │Voice(8084) │...│
   │  MFA/TOTP   │SLA         │WebSocket  │SIP/PBX     │   │
   └─────────────┴────────────┴───────────┴────────────┴───┘
                         │
               ┌─────────▼─────────┐
               │  Kafka + DLQ      │  Dead Letter Topics per service
               │  (Avro schemas)   │  Exponential backoff consumer
               └───────────────────┘
                         │
               ┌─────────▼─────────┐
               │  PostgreSQL (per service, shared) │
               │  tenant_id isolation on all tables│
               └───────────────────────────────────┘
```

## New/Updated Components (v2)

### Per-tenant Rate Limiting
- `PerTenantRateLimiter.java`: In-memory sliding window, keyed by tenantId
- Configurable limits per plan tier (Trial: 50/min, Basic: 200/min, Pro: 500/min, Enterprise: 2000/min)
- Falls back to global limit if no tenant context

### TOTP MFA
- `MfaService.java`: Generates TOTP secrets, verifies 6-digit codes
- QR code provisioning URI for authenticator app integration
- Login flow checks MFA status → challenge with temp token

### Structured Logging
- `MdcLoggingFilter.java`: Sets correlationId, tenantId, userId, traceId in MDC
- `logback-spring.xml`: Logstash JSON encoder, standardized fields
- Service name injected from `spring.application.name`

### SaaS Trial Lifecycle
- `TrialLifecycleService.java`: 14-day trial activation → warning → expiry → conversion
- `FeatureLimitEnforcer.java`: Checks plan limits before operations
- Tenant registration returns trial end date + plan limits

### Dead Letter Queue
- `KafkaDlqConfig.java`: DLQ per topic naming pattern `{topic}.DLQ`
- Exponential backoff: 1s → 2s → 4s → ... → 10s max, 60s max elapsed
- Non-retryable exceptions skip retry

## Resilience Patterns (Updated)
| Pattern | Config |
|---------|--------|
| Circuit Breaker | 50% failure → open 10s |
| Retry | 3 attempts, exponential backoff |
| Bulkhead | 10 concurrent calls |
| Time Limiter | 4s timeout |
| Rate Limiter (global) | 100 req/s per route |
| Rate Limiter (per-tenant) | Plan-based: 50-2000 req/min |

## Observability Stack
- **Logging**: Logstash JSON → Filebeat → Elasticsearch
- **Tracing**: OpenTelemetry OTLP → Jaeger (1% sampling production)
- **Metrics**: Micrometer → Prometheus → Grafana
- **Alerts**: PagerDuty (critical), Slack (warning), Email (info)
