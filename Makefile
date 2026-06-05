.PHONY: build build-all clean build-docker up down deploy test

# Build all services
build-all:
	@echo "Building all services..."
	mvn clean package -DskipTests

# Build specific service
build:
	@echo "Building $(SERVICE)..."
	mvn clean package -pl $(SERVICE) -am -DskipTests

# Clean all
clean:
	mvn clean
	find . -name "target" -type d -exec rm -rf {} +

# Build Docker images - Contact Center services
build-docker-contact:
	@echo "Building Contact Center Docker images..."
	@for service in service-registry config-server api-gateway iam-service agent-service customer-service crm-service call-service sip-service pbx-service recording-service omnichannel-service chat-service email-service sms-service ticket-service campaign-service billing-service cdr-service reporting-service notification-service audit-service; do \
		echo "Building $$service..."; \
		docker build -t vcall/$$service:latest ./$$service; \
	done

# Build Docker images - XR Platform services
build-docker-xr:
	@echo "Building XR Platform Docker images..."
	@for service in tenant-service asset-service scene-service video-service streaming-service xr-service ai-service analytics-service collaboration-service digital-twin-service bim-cad-service gis-service; do \
		echo "Building $$service..."; \
		docker build -t vcall/$$service:latest ./$$service; \
	done

# Build all Docker images
build-docker: build-docker-contact build-docker-xr

# Start infrastructure
infra-up:
	docker-compose -f infra/docker-compose.yml up -d

# Stop infrastructure
infra-down:
	docker-compose -f infra/docker-compose.yml down

# Start all services
up:
	@echo "Starting all services..."
	@for service in service-registry config-server api-gateway iam-service agent-service customer-service crm-service call-service sip-service pbx-service recording-service omnichannel-service chat-service email-service sms-service ticket-service campaign-service billing-service cdr-service reporting-service notification-service audit-service tenant-service asset-service scene-service video-service streaming-service xr-service ai-service analytics-service collaboration-service digital-twin-service bim-cad-service gis-service; do \
		echo "Starting $$service..."; \
		cd $$service && java -jar target/*.jar & \
		cd ..; \
	done

# Deploy to Kubernetes
deploy:
	kubectl apply -k infra/k8s/

# Run tests
test:
	mvn test

# Frontend
frontend-dev:
	cd frontend && npm run dev

frontend-build:
	cd frontend && npm run build

# Help
help:
	@echo "Available targets:"
	@echo "  build-all           - Build all services"
	@echo "  build               - Build a specific service (SERVICE=service-name)"
	@echo "  clean               - Clean all build artifacts"
	@echo "  build-docker        - Build Docker images for all services"
	@echo "  build-docker-contact - Build Contact Center Docker images"
	@echo "  build-docker-xr     - Build XR Platform Docker images"
	@echo "  infra-up            - Start infrastructure (PostgreSQL, Redis, Kafka, MongoDB, etc.)"
	@echo "  infra-down          - Stop infrastructure"
	@echo "  up                  - Start all services locally"
	@echo "  deploy              - Deploy to Kubernetes"
	@echo "  test                - Run tests"
	@echo "  frontend-dev        - Start Next.js dev server"
	@echo "  frontend-build      - Build Next.js frontend"
