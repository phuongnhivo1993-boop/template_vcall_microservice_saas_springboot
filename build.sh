#!/bin/bash
set -e

echo "============================================"
echo "  VCall Contact Center - Build Script"
echo "============================================"

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 21 ]; then
    echo "ERROR: Java 21+ required. Current: $(java -version 2>&1 | head -1)"
    exit 1
fi
echo "Java version OK: $(java -version 2>&1 | head -1)"

# Check Maven
if ! command -v mvn &> /dev/null; then
    echo "ERROR: Maven not found"
    exit 1
fi
echo "Maven version OK: $(mvn --version 2>&1 | head -1)"

# Build common module first
echo ""
echo ">>> Building common module..."
mvn clean install -pl common -DskipTests

# Build all services
echo ""
echo ">>> Building all services..."
mvn clean package -DskipTests

echo ""
echo "============================================"
echo "  Build completed successfully!"
echo "============================================"
echo ""
echo "To start infrastructure: docker-compose -f infra/docker-compose.yml up -d"
echo "To start services:       make up"
echo "To deploy to Kubernetes: kubectl apply -k infra/k8s/"
