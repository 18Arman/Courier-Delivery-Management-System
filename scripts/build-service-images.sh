#!/bin/zsh

set -e

PROJECT_DIR="/Users/armansoni/Spring Project/CouriesDeleiveryManagement"

cd "$PROJECT_DIR"

echo "Building SmartCourier service images..."
docker compose build eureka-server config-server auth-service delivery-service tracking-service admin-service api-gateway

echo "Docker images built successfully."
