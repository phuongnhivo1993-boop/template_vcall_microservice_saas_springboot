# UI/UX Specification

## Design System
- **Framework**: Next.js 14 + Tailwind CSS + shadcn/ui
- **Responsive**: Desktop-first, tablet-adaptive
- **Accessibility**: WCAG 2.1 AA minimum
- **Theme**: Light/Dark mode with system preference detection

## Screen Inventory

### 1. Login Screen
- **URL**: `/login`
- **Elements**: Company/tenant input, username, password, "Forgot Password?", MFA input when enabled
- **States**: Idle, validating, locked-out (423 retry-after), MFA challenge
- **Error**: Generic "Invalid credentials" (prevent enumeration)

### 2. Reset Password Flow
- **URL**: `/forgot-password`, `/reset-password?token=xxx`
- **Flow**: Enter email → success message → check email → new password form → success → redirect to login
- **Validation**: Password strength indicator (real-time policy check)

### 3. Agent Desktop
- **URL**: `/agent`
- **Sections**:
  - Top: Agent status bar (ONLINE/OFFLINE/BUSY/AWAY) with toggle
  - Left: Navigation (Inbox, Tickets, Customers, Reports, Settings)
  - Center: Main workspace (conversation panel / ticket detail)
  - Right: Customer 360 sidebar (click to open)
  - Bottom: Active interactions tray (minimized tabs)
- **States**: No active interaction (empty state), ringing (inbound pop), connected (in-progress), wrap-up (post-call)

### 4. Supervisor Dashboard
- **URL**: `/supervisor`
- **Sections**:
  - Top: Real-time KPIs (active calls, waiting queue, SLA status, agents online)
  - Middle: Agent status grid (color-coded cards, real-time WebSocket updates)
  - Bottom: Team performance charts (CSAT, AHT, FCR)

### 5. Ticket List
- **URL**: `/tickets`
- **Features**: Search by keyword, Filter by status/priority/assignee/date range, Sort by created/priority/SLA deadline, Pagination 20/page

### 6. Admin Panel
- **URL**: `/admin`
- **Sections**: User management, Tenant settings, Billing, Audit log viewer, System health
