# NFR Addendum — Performance, Security, Operations, Compliance

## 1. Performance Benchmarks

| Metric | Target (p95) | Target (p99) | Measurement |
|--------|-------------|-------------|------------|
| Auth Login | <100ms | <300ms | POST /auth/login |
| Token Refresh | <50ms | <150ms | POST /auth/refresh |
| Ticket Create | <200ms | <500ms | POST /tickets |
| Ticket List | <300ms | <800ms | GET /tickets?page=1&size=20 |
| Agent Status Change | <100ms | <300ms | WebSocket broadcast |
| Call Initiation | <500ms | <1500ms | SIP INVITE → RINGING |
| Report Generation | <3s | <10s | GET /reports/calls/summary |
| Knowledge Base Search | <200ms | <500ms | GET /kb/search?q=... |
| Tenant Provisioning | <5min | <15min | POST /tenants/register |
| File Upload (5MB) | <2s | <5s | POST /files/upload |
| GDPR Data Export | <10s | <30s | GET /gdpr/export/{id} |
| WebSocket Message Latency | <50ms | <100ms | Agent → Broker → Subscriber |

**Concurrency Targets**:
- Sustained: 500 concurrent agents, 2000 concurrent customers
- Peak: 1000 agents, 5000 customers (Black Friday / pandemic surge)
- Throughput: 10,000 interactions/hour (voice, chat, email, SMS combined)

## 2. Availability & Disaster Recovery

| Metric | Target |
|--------|--------|
| Uptime SLA | 99.9% (8.76h downtime/year) |
| RPO (Recovery Point Objective) | 1 hour |
| RTO (Recovery Time Objective) | 4 hours |
| Deployment Strategy | Rolling update (zero-downtime) |
| Multi-AZ | Minimum 3 availability zones |
| DB Backup | Daily automated snapshot, 30-day retention |
| Cross-Region DR | Active-passive with manual failover |

**Recovery Runbook**:
1. Detect failure (5 consecutive health check failures or alert)
2. Route traffic to standby region via DNS/CNAME change
3. Promote standby DB replica to primary
4. Verify service health on all endpoints
5. Root cause analysis within 24h

## 3. Security Requirements

| Control | Requirement | Verification |
|---------|-------------|-------------|
| Penetration Testing | Annual, by accredited third-party | Test report |
| SAST (Static Analysis) | In CI pipeline, fail on critical/high | SonarQube gate |
| DAST (Dynamic Analysis) | Weekly scan on staging | OWASP ZAP report |
| Dependency Scanning | Every build, fail on critical CVEs | OWASP Dependency Check |
| Secrets Rotation | Database: 90 days, Encryption key: 180 days, API keys: on demand | Audit log |
| Container Scanning | Every image build, fail on critical/high | Trivy / Snyk |
| Vulnerability Window | Critical: 24h, High: 7d, Medium: 30d | SLA in security policy |

## 4. Observability Requirements

### Logging
- Format: Structured JSON (Logstash schema)
- Fields: `@timestamp`, `level`, `service`, `traceId`, `spanId`, `tenantId`, `userId`, `correlationId`, `message`, `exception`
- Retention: Hot storage 30 days, Warm 90 days, Cold 1 year
- Shipping: Filebeat DaemonSet in K8s → Logstash → Elasticsearch

### Tracing
- Provider: OpenTelemetry SDK (OTLP gRPC)
- Backend: Jaeger or Grafana Tempo
- Sampling: Head-based, 1% production, 100% staging
- Trace context propagation: W3C TraceContext (traceparent header)
- Service graph: All 27 services in trace topology

### Metrics
- Framework: Micrometer + Prometheus
- Retention: Prometheus 15 days, Thanos for long-term
- Custom metrics per service:
  - `vcalls_{service}_requests_total` (by endpoint, status, tenant)
  - `vcalls_{service}_request_duration_seconds` (p50, p95, p99)
  - `vcalls_{service}_errors_total` (by error type)
  - `vcalls_kafka_lag` (per consumer group, per partition)
  - `vcalls_db_connection_pool_usage`

### Alerting
| Alert Rule | Condition | Severity | Channel |
|-----------|-----------|----------|---------|
| Service Down | Health check fails >2min | Critical | PagerDuty + Slack |
| p95 Latency Breach | >500ms for 5min | Warning | Slack |
| Error Rate Spike | >5% for 5min | Critical | PagerDuty + Slack |
| Circuit Breaker Open | >30s | Critical | PagerDuty |
| Kafka Consumer Lag | >1000 messages | Warning | Slack |
| DB Connection Saturation | Pool >80% for 5min | Warning | Slack |
| Rate Limit Reached | >100 429 responses/min | Warning | Slack |
| Certificate Expiry | <30 days | Warning | Email |
| Disk Space | >80% | Warning | Email |

## 5. HIPAA/GDPR Compliance Checklist

| Requirement | Status | Evidence |
|-------------|--------|----------|
| Access Control (RBAC) | ✅ | Role-based endpoint security |
| Audit Logs | ✅ | AuditService + LoginAttempts |
| Encryption at Rest | ✅ | AES-256-GCM via EncryptionUtil |
| Encryption in Transit | ✅ | TLS for all external traffic |
| PII Identification | ✅ | @PII annotation on sensitive fields |
| Breach Notification | ❌ | Need notification workflow |
| BAA Agreements | ❌ | Not implemented |
| Consent Management | ❌ | Not implemented |
| Data Retention Policy | ❌ | Not documented |
| Right to Access (GDPR) | ✅ | GET /gdpr/export |
| Right to Erasure (GDPR) | ✅ | DELETE /gdpr/delete |
| Data Portability | ✅ | JSON export format |
| Privacy by Design | ✅ | PII field tagging |
| DPA (Data Processing Agreement) | ❌ | Not implemented |

## 6. Backup & Retention Policy

| Asset | Backup Frequency | Retention | Method |
|-------|-----------------|-----------|--------|
| PostgreSQL DB (all services) | Daily | 30 days | pg_dump → S3 |
| Kafka topics (critical) | Continuous replication | 7 days | Cross-cluster mirroring |
| Call recordings | Immediate | 1 year | S3 lifecycle policy |
| Log files | Continuous shipping | 1 year | ELK cluster |
| Application config | On change | git history | GitOps |
| Encryption keys | On rotation | 5 years | HSM backup |

## 7. Load Testing Targets

| Scenario | Concurrency | Duration | Throughput Target |
|----------|------------|----------|-------------------|
| Peak hour login burst | 100 users/sec | 30 min | 180,000 logins |
| Normal agent operations | 500 agents | 8 hours | 10,000 interactions |
| Ticket management surge | 200 concurrent creates | 15 min | 5,000 tickets |
| Report generation | 50 concurrent requests | 5 min | 500 reports |
| WebSocket connections | 2000 concurrent | 1 hour | 100,000 messages |
| Bulk API (import/export) | 10 concurrent | 30 min | 100,000 records |

## 8. Data Migration Strategy

1. **Source Analysis**: Document all source systems, data volumes, schema differences
2. **Transformation Rules**: Field-by-field mapping, data cleansing rules, default values
3. **Extraction**: Incremental CDC via Debezium or batch export with timestamp tracking
4. **Validation**: Row count comparison, checksum verification, sample record review
5. **Cutover Plan**: T-7d freeze, T-1d final sync, T-0h switch DNS, T+1d validation
6. **Rollback Plan**: DNS flip-back to old system, verify data integrity, communicate delay
