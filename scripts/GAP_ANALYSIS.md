# VCall Contact Center - Gap Analysis Report

> Phân tích khoảng cách giữa Backend, Admin Web và Mobile App

---

## 1. Tổng quan

| Thành phần | Số lượng |
|---|---|
| Backend controllers | ~64 controller (19 services) |
| Frontend pages | 7 pages (/dashboard, /calls, /agents, /customers, /tickets, /reports, /settings) |
| Mobile screens | 4 tabs (Calls, Chat, Tickets, Profile) |
| Sidebar menu items | 11 items (thiếu page: CRM, Campaigns, Billing, Audit) |

---

## 2. Backend CRUD đầy đủ - Frontend/Mobile chưa có

### 2.1. Chưa có Page nào (cả Web + Mobile)

| Service | Controllers | Số endpoints |
|---|---|---|
| **CRM** | LeadController, OpportunityController, ActivityController, CustomerNoteController | ~24 |
| **Campaign** | CampaignController, CampaignMemberController, CampaignResultController | ~15 |
| **Billing** | InvoiceController, SubscriptionController, PricingPlanController, UsageRecordController | ~16 |
| **Audit** | AuditLogController, SecurityLogController, FraudAlertController, ReconciliationController | ~14 |
| **PBX** | ExtensionController, PbxQueueController, RingGroupController | ~18 |
| **SIP** | SipAccountController, SipDeviceController, SipRegistrationController | ~12 |
| **SMS** | SmsController, SmsTemplateController, SmsProviderController, SmsCampaignController | ~15 |
| **Email** | EmailController, EmailTemplateController, EmailAccountController | ~12 |
| **Notification** | NotificationController, NotificationPreferenceController, NotificationTemplateController, PushDeviceController | ~12 |
| **Omnichannel** | ConversationController, MessageController, ChannelConfigController, RoutingRuleController | ~12 |
| **CDR** | CdrController, CdrAnalyticsController, CdrImportController, CdrSummaryController | ~12 |
| **Recording** | RecordingController, RetentionPolicyController | ~8 |
| **IAM (Roles/Users mgmt)** | RoleController (admin page), UserController (admin page) | ~12 |
| **Call Queue/IVR/Routing** | QueueController, IvrFlowController, RoutingRuleController | ~14 |
| **Agent Groups/Sessions** | AgentGroupController, AgentSessionController | ~8 |
| **Customer Tags** | CustomerTagController | ~5 |
| **SLA Rules** | SlaRuleController | ~7 |

### 2.2. Backend có - Frontend/Mobile thiếu CRUD cụ thể

| Service | Backend có | Frontend có | Mobile có | Thiếu |
|---|---|---|---|---|
| **Agents** | Full CRUD | List (R), Status (U) | - | Create, Edit Details, Delete, Detail view |
| **Customers** | Full CRUD + Contacts + Addresses | List (R), Create (C) | Search (R) | Edit, Delete, Detail, Contacts, Addresses |
| **Tickets** | Full CRUD + Comments + SLA | List (R), Status (U) | List (R), Detail (R), Comment (C) | Create, Assign, Escalate, Delete |
| **Calls** | Create, Read, Update, Hangup | List mock (R) | List mock (R), Dialer mock | Real API integration, Detail view |
| **Chat** | Conversations CRUD + Messages | - | List (R), Messages (R/C) | Assign, Close conversation |
| **Auth** | Login, Refresh, Logout | Login UI | Login UI | /auth/me endpoint (thiếu backend) |

---

## 3. Frontend/Mobile gọi API không có Backend

### Frontend gọi - Backend chưa có

| API | File |
|---|---|
| `GET /auth/me` | `frontend/src/lib/api/index.ts:8` |
| `GET /agents/{id}/sessions` | `frontend/src/lib/api/index.ts:27` |
| `GET /agents/{id}/stats` | `frontend/src/lib/api/index.ts:28` |
| `POST /calls/start` | `frontend/src/lib/api/index.ts:43` |
| `POST /calls/{id}/end` | `frontend/src/lib/api/index.ts:44` |
| `POST /calls/{id}/transfer` | `frontend/src/lib/api/index.ts:45-46` |
| `GET /calls/{id}/recording` | `frontend/src/lib/api/index.ts:47` |
| `GET /calls/stats` | `frontend/src/lib/api/index.ts:48` |
| `POST /reports/generate` | `frontend/src/lib/api/index.ts:64` |
| `GET /reports/{id}/download` | `frontend/src/lib/api/index.ts:67` |
| `GET /audit-logs/export` | `frontend/src/lib/api/index.ts:109-110` |
| `GET /settings/profile` | `frontend/src/lib/api/index.ts:95` |
| `PUT /settings/profile` | `frontend/src/lib/api/index.ts:96` |
| `GET /settings/organization` | `frontend/src/lib/api/index.ts:97` |
| `PUT /settings/organization` | `frontend/src/lib/api/index.ts:98-99` |
| `GET /settings/security` | `frontend/src/lib/api/index.ts:102` |
| `PUT /settings/security` | `frontend/src/lib/api/index.ts:103` |

### Mobile gọi - Backend chưa có

| API | File |
|---|---|
| `GET /auth/me` | `mobile/src/lib/api/index.ts:7` |
| `POST /calls/{id}/mute` | `mobile/src/lib/api/index.ts:17` |
| `POST /calls/{id}/unmute` | `mobile/src/lib/api/index.ts:18` |
| `POST /calls/{id}/hold` | `mobile/src/lib/api/index.ts:19` |
| `POST /calls/{id}/resume` | `mobile/src/lib/api/index.ts:20` |
| `GET /agents/profile` | `mobile/src/lib/api/index.ts:24` |
| `PATCH /agents/status` | `mobile/src/lib/api/index.ts:25` |
| `GET /agents/stats` | `mobile/src/lib/api/index.ts:26` |
| `POST /chat/conversations/{id}/read` | `mobile/src/lib/api/index.ts:50-51` |

---

## 4. Frontend Pages sử dụng Mock Data (chưa kết nối Backend)

| Page | Trạng thái |
|---|---|
| `/dashboard` | Mock charts & statistics |
| `/calls` | Mock table data |
| `/agents` | Mock table data |
| `/customers` | Mock table data; Create modal hoạt động độc lập |
| `/tickets` | Mock table data |
| `/reports` | Mock charts |
| `/settings` | Form UI (chưa kết nối API) |
| `/auth/login` | Login form UI (chưa kết nối API) |

---

## 5. Khuyến nghị

### Priority 1 - Backend cần bổ sung
- `GET /auth/me` - endpoint lấy thông tin current user
- `POST /calls/start`, `POST /calls/{id}/end`, `POST /calls/{id}/transfer`
- `GET /calls/{id}/recording`, `GET /calls/stats`
- `POST /calls/{id}/mute`, `/unmute`, `/hold`, `/resume`
- `GET /agents/profile`, `PATCH /agents/status`, `GET /agents/stats`
- `POST /chat/conversations/{id}/read`
- `GET /settings/profile`, `/organization`, `/security` (PUT tương ứng)
- `GET /audit-logs/export`, `POST /reports/generate`, `GET /reports/{id}/download`

### Priority 2 - Frontend cần xây dựng page
- CRM (Leads, Opportunities, Activities)
- Campaigns (quản lý chiến dịch)
- Billing (Invoices, Plans, Usage)
- Audit (Audit Logs)
- User Management (IAM)
- Role Management

### Priority 3 - Mobile cần bổ sung
- Agents management
- Customers full CRUD
- Customers search
- Notifications screen

---

## 6. File test script

E2E test script: `scripts/e2e-flow-test.sh`

Flow test:
1. Tạo SUPER_ADMIN user (Host)
2. Login Host
3. Tạo Roles (SUPERVISOR, AGENT)
4. Tạo Tenant users (admin + agent)
5. Login Tenant
6. CRUD Agent service
7. CRUD Customer service
8. CRUD Ticket service
9. CRUD Campaign service
10. CRUD User service
