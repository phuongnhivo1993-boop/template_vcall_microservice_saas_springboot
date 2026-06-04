# VCall Contact Center - Features

## Core Platform Features

### Service Discovery & Configuration
- **Eureka Service Registry** (service-registry): Service discovery for all microservices
- **Spring Cloud Config Server** (config-server): Centralized configuration management
- **API Gateway** (api-gateway): Single entry point routing to all services

### Identity & Access Management
- **Authentication & Authorization** (iam-service): 
  - User management with roles and permissions
  - JWT-based authentication
  - Password encoding and security
  - SuperBuilder pattern for entity creation
  - User and Role entities with proper relationships
  - Avatar handling (with noted unmapped properties)

### Agent Management
- **Agent Service** (agent-service):
  - Agent profile management
  - Session tracking
  - Agent status management

### Customer Management
- **Customer Service** (customer-service):
  - Customer profile management
  - Customer contact information
  - Customer segmentation

### CRM Features
- **CRM Service** (crm-service):
  - Lead management
  - Opportunity tracking
  - Sales pipeline management

### Core Contact Center Features
- **Call Service** (call-service):
  - Call control and routing
  - Queue management
  - IVR (Interactive Voice Response)
  - ACD (Automatic Call Distribution)
  - Call state management (started, answered, ended, recorded)

### Communication Channels
- **SIP Service** (sip-service):
  - SIP account management
  - SIP registration handling
  
- **PBX Service** (pbx-service):
  - IP PBX functionality
  - Extension management
  
- **Omnichannel Service** (omnichannel-service):
  - Multi-channel conversation routing
  - Channel-specific routing rules
  - Agent assignment based on skills/availability
  
- **Chat Service** (chat-service):
  - Website and mobile chat support
  - Real-time messaging via WebSocket
  
- **Email Service** (email-service):
  - Inbound and outbound email handling
  - Email templating
  
- **SMS Service** (sms-service):
  - Brand name SMS sending
  - Delivery status tracking

### Ticketing & Issue Management
- **Ticket Service** (ticket-service):
  - Ticket creation and management
  - SLA (Service Level Agreement) tracking
  - Escalation workflows
  - Priority management

### Marketing & Outreach
- **Campaign Service** (campaign-service):
  - Auto dialer campaigns
  - Campaign scheduling and management
  - Contact list management

### Financial & Billing
- **Billing Service** (billing-service):
  - Pricing management
  - Invoice generation
  - Payment processing
  - Billing cycles

### Analytics & Reporting
- **CDR Service** (cdr-service):
  - Call Detail Records storage and retrieval
  - Integration with ClickHouse for large-scale analytics
  
- **Reporting Service** (reporting-service):
  - Dashboard and report generation
  - Real-time analytics
  - Historical reporting

### Operational Features
- **Notification Service** (notification-service):
  - Multi-channel notifications (SMS, Email, Push)
  - Template-based notifications
  
- **Audit Service** (audit-service):
  - Audit logging for compliance
  - Fraud detection mechanisms
  - Activity tracking

### Scheduling & Workforce Management
- **Scheduling Service** (scheduling-service):
  - Agent shift scheduling
  - Break management
  - Time-off requests

### Feedback & Surveys
- **Survey Service** (survey-service):
  - Customer satisfaction surveys
  - Post-call surveys
  - Feedback collection and analysis

## Technical Features

### Event-Driven Architecture
- **Apache Kafka Integration**: Event streaming between services
- **Event Types**:
  - Agent events: created, status changed
  - Customer events: created, updated
  - Call events: started, answered, ended, recorded
  - CDR events: generated
  - Ticket events: created, closed, escalated
  - Chat events: started, closed, message sent
  - Email events: received, sent
  - SMS events: sent, delivered, failed
  - Campaign events: started, finished
  - Billing events: invoice created, payment completed
  - SLA events: breach notifications
  - Lead events: created, converted

### Data Storage & Persistence
- **Polyglot Persistence**:
  - PostgreSQL for transactional data (per service)
  - ClickHouse for analytical data (CDR)
  - Redis for caching
  - Elasticsearch for search capabilities
  - MinIO for object storage (recordings, attachments)

### Security Features
- **Spring Security**: Authentication and authorization framework
- **JWT Tokens**: Stateless authentication mechanism
- **Role-Based Access Control**: Fine-grained permissions
- **Input Validation**: Using Jakarta Validation
- **Password Encoding**: Secure password storage

### API Documentation
- **SpringDoc OpenAPI**: Automatic API documentation generation
- **Swagger UI**: Interactive API documentation interface
- **API Endpoints**: Comprehensive RESTful APIs for all services

### Monitoring & Observability
- **Spring Actuator**: Health checks, metrics, and monitoring
- **Prometheus Integration**: Metrics collection and exposition
- **Health Check Endpoints**: Service-level health monitoring
- **Logging**: Structured logging with SLF4J

### Development & Deployment
- **Maven Multi-module Build**: Consistent build process
- **Docker Support**: Containerization for deployment
- **Kubernetes Manifests**: Cloud-native deployment ready
- **Configuration Externalization**: Environment-specific configs

### Code Quality & Standards
- **Lombok**: Reduced boilerplate code
- **MapStruct**: Object mapping automation
- **Spring Boot 3.2.5**: Latest Spring Boot features
- **Java 21**: Modern Java language features
- **Spring Cloud 2023.0.1**: Latest Spring Cloud ecosystem

## Voice Platform Integration
- **Kamailio**: High-performance SIP server/proxy
- **FreeSWITCH/Asterisk**: Media server for voice processing
- **WebRTC Support**: Browser-based voice communication
- **Codec Support**: Multiple audio codecs for compatibility

## Recording & Storage
- **Call Recording**: Automatic and on-demand recording
- **Multiple Formats**: WAV, MP3, OGG support
- **Secure Storage**: Encrypted storage options
- **Retention Policies**: Configurable recording retention
- **Playback & Download**: Easy access to recordings

## Limitations & Known Issues (from build process)
- Some services have test compilation issues due to deprecated Spring Test annotations
- RecordingService had controller mapping conflicts (fixed)
- OmnichannelService missing UUID import (fixed)
- Various Lombok warnings about SuperBuilder usage
- Unmapped properties in UserMapper (avatar field)
- Some services use deprecated APIs that need updating

## Future Enhancements
- AI-powered call analytics and sentiment analysis
- Advanced workforce management with forecasting
- Integration with popular CRM platforms (Salesforce, HubSpot)
- Advanced IVR with natural language processing
- Real-time agent assistance and coaching
- Quality management and call scoring
- Outbound predictive dialing
- Social media channel integration
- Advanced reporting and custom dashboard builder