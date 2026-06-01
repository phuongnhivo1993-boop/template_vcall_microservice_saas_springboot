# VCall Contact Center - Hệ thống Contact Center SaaS

## Tổng quan

Hệ thống Contact Center Healthcare đa kênh (Omnichannel) dạng SaaS, phục vụ các doanh nghiệp như Medihub, bệnh viện, phòng khám hoặc tổng đài chăm sóc khách hàng quy mô lớn.

## Công nghệ sử dụng

### Backend
- **Java 21** + **Spring Boot 3.2.5**
- **Spring Cloud 2023.0.1** (Gateway, Config, Eureka)
- **Spring Data JPA** + **PostgreSQL** (mỗi service 1 DB riêng)
- **Apache Kafka** (Event-driven architecture)
- **Spring Security** + **JWT** (Authentication/Authorization)

### Database & Storage
- **PostgreSQL 16** - OLTP Database (mỗi service 1 database)
- **ClickHouse** - CDR Analytics (lưu CDR scale lớn)
- **Redis 7** - Cache
- **ElasticSearch 8** - Search
- **MinIO** - File storage (recording, attachments)

### Voice Platform
- **Kamailio** - SIP Server/Proxy
- **FreeSWITCH / Asterisk** - Media Server

### Frontend
- **Next.js 14** (Web App)
- **React Native + Expo Router** (Mobile App)

## Kiến trúc Microservices

| Domain | Service | Port | Database | Mô tả |
|--------|---------|------|----------|-------|
| **Platform** | service-registry | 8761 | - | Eureka Service Discovery |
| **Platform** | config-server | 8888 | - | Spring Cloud Config |
| **Platform** | api-gateway | 8080 | - | API Gateway (routes all services) |
| **Identity** | iam-service | 8101 | vcall_iam | Authentication, Users, Roles |
| **Agent** | agent-service | 8102 | vcall_agent | Agent management, sessions |
| **Customer** | customer-service | 8103 | vcall_customer | Customer management |
| **CRM** | crm-service | 8104 | vcall_crm | Leads, Opportunities |
| **Core Voice** | call-service | 8105 | vcall_call | Call control, Queue, IVR, ACD |
| **SIP** | sip-service | 8106 | vcall_sip | SIP accounts, registration |
| **PBX** | pbx-service | 8107 | vcall_pbx | IP PBX, Extensions |
| **Recording** | recording-service | 8108 | vcall_recording | Call recording (MinIO) |
| **Omnichannel** | omnichannel-service | 8109 | vcall_omnichannel | Multi-channel conversations |
| **Chat** | chat-service | 8110 | vcall_chat | Website/Mobile Chat |
| **Email** | email-service | 8111 | vcall_email | Email inbound/outbound |
| **SMS** | sms-service | 8112 | vcall_sms | Brand Name SMS |
| **Ticket** | ticket-service | 8113 | vcall_ticket | Ticket, SLA, Escalation |
| **Campaign** | campaign-service | 8114 | vcall_campaign | Auto Dialer campaigns |
| **Billing** | billing-service | 8115 | vcall_billing | Pricing, Invoicing |
| **CDR** | cdr-service | 8116 | vcall_cdr + ClickHouse | Call Detail Records |
| **Reporting** | reporting-service | 8117 | vcall_reporting | Dashboard, Reports |
| **Notification** | notification-service | 8118 | vcall_notification | SMS/Email/Push notifications |
| **Audit** | audit-service | 8119 | vcall_audit | Audit logging, Fraud detection |

## Kiến trúc Event Driven (Kafka Topics)

- `agent.created`, `agent.status.changed`
- `customer.created`, `customer.updated`
- `call.started`, `call.answered`, `call.ended`, `call.recorded`
- `cdr.generated`
- `ticket.created`, `ticket.closed`, `ticket.escalated`
- `chat.started`, `chat.closed`, `chat.message.sent`
- `email.received`, `email.sent`
- `sms.sent`, `sms.delivered`, `sms.failed`
- `campaign.started`, `campaign.finished`
- `invoice.created`, `payment.completed`
- `sla.breach`
- `lead.created`, `lead.converted`

## Yêu cầu hệ thống

- **Java 21+**
- **Maven 3.9+**
- **Docker & Docker Compose** (cho infrastructure)
- **Node.js 20+** (cho frontend)
- **kubectl** (cho Kubernetes deployment)

## Hướng dẫn chạy

### 1. Clone và build
```bash
git clone <repo-url>
cd template_vcall_microservice_saas_springboot

# Build toàn bộ hệ thống
./build.sh
# hoặc
make build-all
```

### 2. Khởi động Infrastructure
```bash
make infra-up
# Bao gồm: PostgreSQL, Redis, Kafka, ElasticSearch, MinIO, ClickHouse
```

### 3. Khởi động các service
```bash
# Theo thứ tự:
# 1. Service Registry
cd service-registry && java -jar target/*.jar &

# 2. Config Server
cd config-server && java -jar target/*.jar &

# 3. API Gateway
cd api-gateway && java -jar target/*.jar &

# 4. Các microservice (có thể start song song)
cd iam-service && java -jar target/*.jar &
cd agent-service && java -jar target/*.jar &
# ... v.v.

# Hoặc dùng Makefile:
make up
```

### 4. Frontend
```bash
cd frontend
npm install
npm run dev
# Truy cập: http://localhost:3000
```

### 5. Mobile
```bash
cd mobile
npm install
npx expo start
```

### 6. Deploy lên Kubernetes
```bash
make deploy
# hoặc
kubectl apply -k infra/k8s/
```

## API Documentation

Sau khi khởi động API Gateway (port 8080):
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/api-docs

## Monitoring

- **Health Check**: http://localhost:{port}/actuator/health
- **Metrics**: http://localhost:{port}/actuator/metrics
- **Prometheus**: http://localhost:{port}/actuator/prometheus
- **Kafka UI**: http://localhost:8081

## Cấu trúc thư mục

```
├── common/                  # Shared module (DTOs, entities, exceptions)
├── service-registry/        # Eureka Server
├── config-server/           # Spring Cloud Config Server
├── api-gateway/             # API Gateway
├── iam-service/             # Identity & Access Management
├── agent-service/           # Agent Management
├── customer-service/        # Customer Management
├── crm-service/             # CRM (Leads, Opportunities)
├── call-service/            # Contact Center Core
├── sip-service/             # SIP Management
├── pbx-service/             # IP PBX
├── recording-service/       # Call Recording
├── omnichannel-service/     # Omnichannel
├── chat-service/            # Chat
├── email-service/           # Email
├── sms-service/             # SMS
├── ticket-service/          # Ticket Management
├── campaign-service/        # Campaign Auto Dialer
├── billing-service/         # Billing
├── cdr-service/             # CDR
├── reporting-service/       # Reporting
├── notification-service/    # Notifications
├── audit-service/           # Audit
├── frontend/                # Next.js Web App
├── mobile/                  # Expo React Native App
├── infra/                   # Docker, K8s, Scripts
│   ├── docker-compose.yml
│   ├── k8s/                 # Kubernetes manifests
│   ├── init-db.sh
│   ├── clickhouse-init.sql
│   └── minio-init.sh
└── pom.xml                  # Root Maven POM
```
