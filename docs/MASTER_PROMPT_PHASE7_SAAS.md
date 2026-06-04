# Phase 7: SaaS Analysis

## 7.1 Multi-Tenancy Assessment

| Aspect | Current State | Required for SaaS | Priority |
|--------|---------------|-------------------|----------|
| **Data Isolation** | ✅ tenant_id on all BaseEntity entities | tenant_id on every table | P0 |
| **Tenant Identification** | ✅ X-Tenant-Id header + JWT claim + automatic filter | Multi-source tenant extraction | P0 |
| **Tenant Provisioning** | ❌ Manual (DataInitializer) | Self-service signup flow | P1 |
| **Tenant Configuration** | ⚠️ Config server per environment | Per-tenant config overrides | P1 |
| **Tenant Routing** | ❌ API gateway routes by URL only | Subdomain-based routing (tenant.vcall.com) | P2 |
| **Resource Limits** | ❌ Not implemented | Per-tenant rate limiting (done at gateway level) | P0 |
| **Usage Metrics** | ❌ Not implemented | Metering for billing | P1 |

## 7.2 Tenant Data Isolation

```sql
-- Every table has tenant_id column (via BaseEntity)
-- Queries automatically filtered:
SELECT * FROM tickets WHERE tenant_id = 'tenant-abc' AND is_deleted = false;
-- Filtering via TenantFilter + TenantContext (ThreadLocal)
-- Hibernate filter can be enabled per session
```

## 7.3 Subscription & Billing

| Component | Status | Description |
|-----------|--------|-------------|
| Pricing Plans | ✅ | PricingPlan entity with CRUD |
| Invoice Generation | ✅ | Invoice entity, auto-generation |
| Payment Processing | ✅ | Payment entity |
| Subscription Management | **❌** | No subscription lifecycle (trial/active/canceled) |
| Trial Management | **❌** | No trial period logic |
| Usage-Based Billing | **❌** | No metering |
| Tax Calculation | **❌** | No tax logic |
| Discount/Coupon | **❌** | No promotional logic |
| Dunning (failed payments) | **❌** | No retry/notification flow |

### Subscription Model (Proposed)

| Tier | Price | Agents | Channels | Features |
|------|-------|--------|----------|----------|
| **Starter** | $99/mo | 5 | Chat + Email | Basic IVR, Ticket, Reports |
| **Professional** | $299/mo | 25 | Chat + Email + SMS + Voice | Full IVR, SLA, Campaigns |
| **Enterprise** | $999/mo | Unlimited | All channels + Social | AI, Custom, API, HIPAA |
| **Healthcare** | Custom | Unlimited | All + HIPAA | Full compliance, BAA, Audit |

## 7.4 Tenant Onboarding Flow

| Step | Status | Notes |
|------|--------|-------|
| 1. Sign up (email + password) | ❌ | Not implemented |
| 2. Verify email | ❌ | No verification flow |
| 3. Select plan (trial or paid) | ❌ | No plan selection |
| 4. Provision tenant DB/schema | ❌ | No provisioning |
| 5. Create admin user | ❌ | No auto-provisioning |
| 6. Configure channels (SIP, SMS, Email) | ❌ | No setup wizard |
| 7. Start using | ❌ | Not automated |

## 7.5 SaaS Infrastructure Requirements

| Requirement | Status | Implementation |
|-------------|--------|----------------|
| Centralized logging per tenant | ❌ | ELK with tenant index |
| Tenant-level metrics | ❌ | Prometheus labels |
| Usage tracking (API calls, storage, agents) | ❌ | Metering service needed |
| Tenant health monitoring | ❌ | Per-tenant health checks |
| Backup per tenant | ❌ | Separate DB dump |
| Data retention policy | ❌ | Configurable per tenant |

## 7.6 RBAC Matrix (Multi-tenant)

| Permission | SUPER_ADMIN | SUPERVISOR | AGENT | CUSTOMER | FINANCE | QA |
|------------|-------------|------------|-------|----------|---------|-----|
| Manage Users | ✅ | ⚠️ (team) | ❌ | ❌ | ❌ | ❌ |
| View Reports | ✅ | ✅ (team) | ❌ | ⚠️ (own) | ✅ | ✅ |
| Manage Campaigns | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ |
| View Billing | ✅ | ❌ | ❌ | ⚠️ (own) | ✅ | ❌ |
| Listen to Calls | ✅ | ✅ (team) | ❌ | ❌ | ❌ | ✅ |
| Manage Tickets | ✅ | ✅ | ✅ (assigned) | ❌ | ❌ | ❌ |
| Tenant Config | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |

---

*End of Phase 7 — SaaS Completeness Score: 55%*
*Next: Phase 8 — Security Review*
