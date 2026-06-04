# Monitoring & Observability (v2)

## 1. Structured Logging
- **Format**: JSON (Logstash schema via `logstash-logback-encoder`)
- **Required fields**: `@timestamp`, `level`, `service`, `traceId`, `spanId`, `tenantId`, `userId`, `correlationId`, `requestUri`, `requestMethod`, `message`
- **MDC injection**: `MdcLoggingFilter` sets fields from request context
- **Shipment**: Filebeat → Logstash → Elasticsearch
- **Retention**: Hot 30d, Warm 90d, Cold 1yr

## 2. Distributed Tracing
- **Provider**: OpenTelemetry SDK (OTLP gRPC)
- **Backend**: Jaeger
- **Sampling**: 1% production, 100% staging
- **Propagation**: W3C TraceContext (`traceparent` header)

## 3. Metrics (Micrometer + Prometheus)
| Metric | Type | Labels |
|--------|------|--------|
| `vcalls_auth_requests_total` | Counter | status, tenantId |
| `vcalls_auth_login_duration_seconds` | Histogram | |
| `vcalls_ticket_requests_total` | Counter | endpoint, status |
| `vcalls_kafka_messages_total` | Counter | topic, status |
| `vcalls_kafka_dlq_messages_total` | Counter | topic |
| `vcalls_feature_limit_blocked_total` | Counter | limitType, tenantId |
| `jvm_memory_used_bytes` | Gauge | area |
| `hikaricp_connections_active` | Gauge | pool |

## 4. Health Checks
- `/actuator/health` — Liveness + Readiness (default + custom)
- Custom indicators: KafkaHealthIndicator (produce+consume test), RedisHealthIndicator (PING)

## 5. Alerts (Updated)
| Alert | Condition | Severity | Channel |
|-------|-----------|----------|---------|
| Service Down | Health fail >2min | Critical | PagerDuty |
| Auth Error Spike | >50 401/min | Warning | Slack |
| Rate Limit Exceeded | >100 429/min | Warning | Slack |
| DLQ Messages | >0 for >5min | Warning | Slack |
| Trial Expiry | T-3 days | Info | Email |
| Circuit Breaker Open | >30s | Critical | PagerDuty |
| MFA Failure Spike | >10 invalid/min | Warning | Slack |
