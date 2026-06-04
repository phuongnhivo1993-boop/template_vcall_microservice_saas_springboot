# User Stories (v2)

## Epic: Security & MFA

### US-001: User Login with MFA
**As** an admin  
**I want** to log in with password + TOTP code  
**So that** my account is protected by two-factor authentication  
**Acceptance**: Login flow checks MFA enrollment, prompts for 6-digit code, verifies against TOTP secret

### US-002: MFA Enrollment
**As** a user  
**I want** to enable MFA on my account  
**So that** I can add an extra layer of security  
**Acceptance**: QR code displayed, 6-digit code verified before activation, secret stored encrypted

### US-003: MFA Disable
**As** a user  
**I want** to disable MFA using my current code  
**So that** I can remove two-factor if needed  
**Acceptance**: Must provide valid code to disable, admin can force-disable for locked-out users

### US-004: Trial Expiry Warning
**As** a tenant admin  
**I want** to be notified when my trial is about to expire  
**So that** I can upgrade before service suspension  
**Acceptance**: Notification at day 3, 1, and 0 remaining, feature limits enforced during trial

## Epic: Agent Desktop

### US-010: Real-time Status
**As** a supervisor  
**I want** to see all agents' real-time status via WebSocket  
**So that** I can monitor team availability  
**Acceptance**: WebSocket updates <1s, statuses: ONLINE/OFFLINE/BUSY/AWAY/BREAK

### US-011: Handle Inbound Interaction
**As** an agent  
**I want** to handle voice, chat, email, SMS from one workspace  
**So that** I can assist customers efficiently  
**Acceptance**: Unified inbox, channel tabs, customer 360 sidebar, customer info on accept

## Epic: SaaS & Billing

### US-020: Self-service Signup
**As** a new customer  
**I want** to register my company, select a plan, and start a trial  
**So that** I can evaluate the platform before purchasing  
**Acceptance**: Wizard (company info → admin account → plan selection → payment → confirmation)

### US-021: Track Usage
**As** a tenant admin  
**I want** to see my monthly usage vs plan limits  
**So that** I can avoid surprise overage charges  
**Acceptance**: Usage dashboard, % consumed per metric, upgrade CTA when near limit

## Epic: Observability

### US-030: Correlation ID
**As** a support engineer  
**I want** to search logs by correlation ID  
**So that** I can trace a request across all services  
**Acceptance**: X-Correlation-Id header propagated, logged in structured JSON, searchable in Kibana

### US-031: Trace Visualization
**As** a developer  
**I want** to view traces in Jaeger  
**So that** I can debug latency issues across microservices  
**Acceptance**: OpenTelemetry traces exported, service graph visible, span details available
