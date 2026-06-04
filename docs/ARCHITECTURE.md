# VCall Contact Center - Architecture

## High-Level Architecture

The VCall Contact Center follows a microservices architecture with clear separation of concerns, organized by business domains. Each service is independently deployable, scalable, and maintainable.

## Architectural Layers

### 1. Infrastructure Layer
- **Service Discovery**: Eureka Server (service-registry)
- **Configuration Management**: Spring Cloud Config Server (config-server)
- **API Gateway**: Single entry point (api-gateway)
- **Message Broker**: Apache Kafka for event-driven communication
- **Databases**: 
  - PostgreSQL (per service for OLTP)
  - ClickHouse (for CDR analytics)
  - Redis (caching)
  - Elasticsearch (search)
  - MinIO (object storage)
- **Voice Platform**: Kamailio (SIP) + FreeSWITCH/Asterisk (media)

### 2. Platform Services
These services provide foundational capabilities:
- **service-registry**: Service discovery and registration
- **config-server**: Centralized configuration management
- **api-gateway**: Request routing, load balancing, security

### 3. Domain Services
Business capability services organized by domain:

#### Identity & Security Domain
- **iam-service**: Authentication, authorization, user/role management

#### Customer Domain
- **customer-service**: Customer profile and contact management
- **customer360-service**: Unified customer view across channels

#### Sales & Marketing Domain
- **crm-service**: Lead and opportunity management
- **campaign-service**: Auto dialer and marketing campaigns

#### Core Communication Domain
- **call-service**: Call control, queuing, IVR, ACD
- **sip-service**: SIP protocol handling and account management
- **pbx-service**: IP PBX functionality and extension management
- **recording-service**: Call recording storage and retrieval
- **omnichannel-service**: Unified multi-channel conversation handling
- **chat-service**: Real-time text chat (web/mobile)
- **email-service**: Email inbound/outbound processing
- **sms-service**: SMS sending and delivery tracking

#### Operational Domain
- **ticket-service**: Issue tracking, SLA management, escalation
- **billing-service**: Pricing, invoicing, payment processing
- **notification-service**: Multi-channel notifications (SMS/Email/Push)
- **audit-service**: Audit logging, compliance, fraud detection
- **scheduling-service**: Agent shift and workforce management
- **survey-service**: Feedback collection and satisfaction surveys
- **cdr-service**: Call Detail Records storage and analytics
- **reporting-service**: Dashboard, reporting, and business intelligence

### 4. Presentation Layer
- **frontend**: Next.js 14 web application
- **mobile**: React Native + Expo mobile application

## Communication Patterns

### Synchronous Communication
- **RESTful APIs**: Primary communication mechanism between services
- **GraphQL**: Potential for future implementation (not currently used)
- **gRPC**: Internal service-to-service communication where performance is critical

### Asynchronous Communication
- **Apache Kafka**: Event streaming for loose coupling and scalability
  - Each domain publishes relevant events
  - Services subscribe to events they care about
  - Enables eventual consistency and workflow automation

### Data Flow Examples

#### Call Initiation Flow
1. Call comes in via SIP/Kamailio
2. sip-service handles SIP signaling
3. call-service manages call state and routing logic
4. omnichannelservice determines routing based on rules
5. agent-service assigns available agent
6. pbx-service connects media path
7. recording-service starts recording if configured
8. Events published to Kafka: call.started, call.answered, etc.

#### Omnichannel Message Flow
1. Customer sends message via chat/email/sms
2. Respective service (chat/email/sms) receives and validates
3. omnichannelservice creates conversation thread
4. omnichannelservice applies routing rules
5. agent-service assigns agent if needed
6. Notification sent via notification-service
7. Events published: chat.started, email.received, etc.

#### Ticket Creation Flow
1. Interaction creates need for follow-up (call ends, chat completes, etc.)
2. ticket-service creates ticket with context
3. notification-service alerts assigned agent
4. SLA timer starts via scheduling-service
5. Events published: ticket.created, sla.breach (if applicable)

## Data Management Strategy

### Database per Service Pattern
Each microservice owns its data store:
- Loose coupling between services
- Technology flexibility per service
- Independent scaling and deployment
- Clear ownership of data schemas

### Eventual Consistency
- Services communicate via events
- Data synchronization happens asynchronously
- Conflict resolution strategies implemented where needed
- Read models may be denormalized for performance

### Caching Strategy
- Redis used for:
  - Session storage
  - Frequently accessed reference data
  - Rate limiting counters
  - Temporary computation results

### Search & Analytics
- Elasticsearch for:
  - Text search across conversations
  - Customer profile search
  - Knowledge base search
  
- ClickHouse for:
  - CDR analytics at scale
  - Call volume and quality metrics
  - Performance reporting

## Security Architecture

### Authentication Flow
1. User provides credentials to iam-service
2. iam-service validates and issues JWT token
3. Token includes user identity, roles, permissions
4. Services validate token via Spring Security
5. Authorization checked at service/method level

### Authorization Model
- Role-Based Access Control (RBAC)
- Hierarchical roles (admin > supervisor > agent)
- Resource-based permissions
- Field-level security for sensitive data

### Data Protection
- Encryption at rest for sensitive data
- TLS encryption for service-to-service communication
- API gateway handles SSL termination
- Secrets managed via Spring Cloud Config/Vault (planned)

## Deployment Architecture

### Containerization
- Docker containers for all services
- Multi-stage builds for optimized images
- Shared base images for efficiency
- Health checks built into containers

### Orchestration Options
- **Docker Compose**: Development and testing environments
- **Kubernetes**: Production deployments
  - Helm charts for service deployment
  - Istio service mesh (planned for advanced traffic management)
  - Horizontal pod autoscaling based on metrics
  - Rolling updates for zero-downtime deployments

### Environment Strategy
- **Development**: Local Docker Compose with mock services
- **Staging**: Kubernetes with production-like configuration
- **Production**: Kubernetes with monitoring, logging, alerting

## Observability & Monitoring

### Metrics Collection
- Micrometer integration with Spring Boot 3
- Prometheus endpoint exposure on each service
- Custom business metrics alongside standard JVM metrics
- Histograms for latency measurements
- Counters for business events

### Logging Strategy
- Structured JSON logging via Logback/SLF4J
- Correlation IDs passed across service boundaries
- Centralized logging with ELK stack (Elasticsearch, Logstash, Kibana)
- Log levels configurable per service/package

### Distributed Tracing
- OpenTelemetry integration (planned)
- Trace IDs propagated via HTTP headers
- Span creation for service boundaries and database calls
- Visualization via Jaeger or Tempo

### Health Checks
- Liveness probes: Detect deadlocked services
- Readiness probes: Detect when service can accept traffic
- Custom business health checks (database connectivity, queue depth, etc.)
- Circuit breaker patterns for external dependencies

## Resilience Patterns

### Fault Tolerance
- Circuit Breaker: Prevent cascade failures (Resilience4j/Spring Cloud Circuit Breaker)
- Bulkhead: Limit concurrent calls to prevent resource exhaustion
- Retry: Exponential backoff with jitter for transient failures
- Timeout: Prevent hanging calls from consuming resources

### Data Resilience
- Database replication and backups
- Event log replay capability (Kafka serves as durable event log)
- Idempotent event handlers to handle duplicates
- Snapshotting for aggregate states where applicable

### Graceful Degradation
- Non-essential features disabled during high load
- Cached responses when downstream services unavailable
- Fallback messages for temporary outages
- Queue depth monitoring with automatic scaling triggers

## Scalability Characteristics

### Horizontal Scaling
- Stateless services scale horizontally with load balancer
- Stateful services use externalized state (databases, caches)
- Kafka partitioning enables parallel event processing
- Database read replicas for read-heavy operations

### Vertical Scaling
- Resource limits defined per container
- JVM tuning for optimal garbage collection
- Connection pool sizing based on expected load
- Thread pool tuning for concurrent request handling

### Load Distribution
- API Gateway provides load balancing
- Kubernetes services provide internal load balancing
- Kafka consumer groups distribute event processing
- Database connection pooling prevents overload

## Technology Rationale

### Java 21 & Spring Boot 3.2.5
- Long-term support (LTS) release
- Modern language features (records, pattern matching)
- Improved performance and security
- Spring Boot 3 provides native compilation readiness
- Jakarta EE 9+ namespace alignment

### Spring Cloud 2023.0.1
- Latest stable release with improved compatibility
- Better integration with Spring Boot 3
- Enhanced observability features
- Security updates and bug fixes

### Database Choices
- **PostgreSQL**: Reliable, feature-rich OLTP database
- **ClickHouse**: Columnar store optimized for analytics workloads
- **Redis**: In-memory store with persistence options
- **Elasticsearch**: Distributed search and analytics engine
- **MinIO**: S3-compatible object storage for binary data

### Communication Technologies
- **Apache Kafka**: High-throughput, fault-tolerant event streaming
- **REST/JSON**: Widely understood, debuggable, language-agnostic
- **WebSocket**: Real-time bidirectional communication for chat

### Frontend Technologies
- **Next.js 14**: Server-side rendering, routing, API routes
- **React Native**: Cross-platform mobile development
- **Expo**: Simplified mobile development and deployment

## Constraints & Trade-offs

### Consistency vs Availability
- Chooses availability and partition tolerance (AP) in CAP theorem
- Uses eventual consistency model with conflict resolution
- Strong consistency only where absolutely required (financial transactions)

### Complexity vs Functionality
- Microservices increase operational complexity
- Compensated by domain alignment and team autonomy
- Investment in DevOps practices to manage complexity

### Performance vs Development Speed
- Accepts slight latency overhead for service boundaries
- Gains in deployment frequency and team productivity
- Performance optimization focused on critical paths

### Standardization vs Flexibility
- Standardizes on JVM ecosystem for team expertise
- Allows polyglot persistence where beneficial
- Standardizes communication patterns but allows implementation variation

## Future Architectural Considerations

### Service Mesh
- Istio or Linkerd for advanced traffic management
- Mutual TLS for service-to-service encryption
- Fine-grained traffic control and observability

### Event Sourcing & CQRS
- Consider for domains with complex audit requirements
- Separate read and write models for better performance
- Event store as system of record for certain domains

### Edge Computing
- WebSocket edge connections for global low-latency access
- Regional deployment of media servers
- CDN integration for static assets

### AI/ML Integration
- Real-time sentiment analysis during calls
- Predictive routing based on caller history
- Automated quality scoring and coaching suggestions
- Chatbot integration for tier-1 support

### API Evolution
- GraphQL implementation for flexible data fetching
- Webhook ecosystem for third-party integrations
- AsyncAPI documentation for event contracts