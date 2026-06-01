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

# Build Docker images
build-docker:
	@echo "Building Docker images..."
	@for service in service-registry config-server api-gateway iam-service agent-service customer-service crm-service call-service sip-service pbx-service recording-service omnichannel-service chat-service email-service sms-service ticket-service campaign-service billing-service cdr-service reporting-service notification-service audit-service; do \
		echo "Building $$service..."; \
		docker build -t vcall/$$service:latest ./$$service; \
	done

# Start infrastructure
infra-up:
	docker-compose -f infra/docker-compose.yml up -d

# Stop infrastructure
infra-down:
	docker-compose -f infra/docker-compose.yml down

# Start all
up:
	@echo "Starting all services..."
	@for service in service-registry config-server api-gateway iam-service agent-service customer-service crm-service call-service sip-service pbx-service recording-service omnichannel-service chat-service email-service sms-service ticket-service campaign-service billing-service cdr-service reporting-service notification-service audit-service; do \
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

# Help
help:
	@echo "Available targets:"
	@echo "  build-all      - Build all services"
	@echo "  build          - Build a specific service (SERVICE=service-name)"
	@echo "  clean          - Clean all build artifacts"
	@echo "  build-docker   - Build Docker images for all services"
	@echo "  infra-up       - Start infrastructure (PostgreSQL, Redis, Kafka, etc.)"
	@echo "  infra-down     - Stop infrastructure"
	@echo "  up             - Start all services locally"
	@echo "  deploy         - Deploy to Kubernetes"
	@echo "  test           - Run tests"
