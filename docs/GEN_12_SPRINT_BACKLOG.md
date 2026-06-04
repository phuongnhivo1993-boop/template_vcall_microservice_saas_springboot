# Sprint Backlog (v2)

## Sprint 1 (Week 1-2): Security Foundation — COMPLETED
| Item | Status |
|------|--------|
| PasswordPolicy bean with configurable rules | ✅ |
| LoginAttemptService + lockout logic | ✅ |
| EncryptionUtil (AES-256-GCM) | ✅ |
| Account lockout in AuthService | ✅ |
| Password validation in UserService | ✅ |
| Bulkhead in Resilience4jConfig | ✅ |
| Unit tests | ✅ |

## Sprint 2 (Week 3-4): MFA + Infrastructure — COMPLETED
| Item | Status |
|------|--------|
| TOTP MFA setup/verify/disable | ✅ |
| MfaController endpoints | ✅ |
| MfaService with QR code provisioning | ✅ |
| Login flow with MFA challenge | ✅ |
| API versioning (/api/v1) | ✅ |
| Kafka DLQ config | ✅ |
| Tenant registration API + trial activation | ✅ |
| Per-tenant rate limiter | ✅ |
| Structured logging with MDC | ✅ |
| Feature limits enforcement | ✅ |

## Sprint 3 (Week 5-6): UX & Frontend
| Item | Effort | Assignee |
|------|--------|----------|
| Login screen (Next.js) with MFA | 3d | frontend |
| Password reset UI | 2d | frontend |
| Agent desktop (Next.js) | 5d | frontend |
| Supervisor dashboard | 3d | frontend |
| Tenant self-service signup wizard | 3d | frontend |
| Usage/billing dashboard | 2d | frontend |
| Mobile login + MFA (React Native) | 3d | mobile |
| Mobile ticket list | 3d | mobile |

## Sprint 4 (Week 7-8): SaaS & Compliance
| Item | Effort | Assignee |
|------|--------|----------|
| Subscription billing (Stripe) | 3d | billing |
| Trial expiry notification | 2d | notif |
| Usage-based billing aggregation | 3d | billing |
| Dunning (failed payment retry) | 2d | billing |
| Feature limit enforcement UI | 2d | frontend |
| Admin audit log viewer | 2d | frontend |
| HIPAA BA agreement tracking | 2d | iam |
| Consent management | 2d | iam |

## Sprint 5 (Week 9-10): Advanced Features
| Item | Effort | Assignee |
|------|--------|----------|
| Knowledge base service | 3d | backend |
| KB search + management UI | 3d | frontend |
| Campaign predictive dialer | 3d | backend |
| Campaign creation wizard | 2d | frontend |
| Social channel adapters (Zalo, FB) | 3d | backend |
| Chatbot integration (Dialogflow) | 4d | backend |
| IVR flow designer (drag-drop) | 5d | frontend |

## Sprint 6 (Week 11-12): Polish & Performance
| Item | Effort | Assignee |
|------|--------|----------|
| Load testing (Gatling) | 3d | qa |
| HPA/PDB K8s config | 2d | devops |
| Vault integration | 2d | backend |
| EAS Build pipeline | 2d | mobile |
| Performance optimization | 3d | all |
| Bug fixes | 3d | all |
