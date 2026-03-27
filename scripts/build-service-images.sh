#!/bin/zsh

set -e

PROJECT_DIR="/Users/armansoni/Spring Project/CouriesDeleiveryManagement"
DEFAULT_SERVICES=(eureka-server config-server auth-service delivery-service tracking-service admin-service api-gateway)

cd "$PROJECT_DIR"

if [ "$#" -gt 0 ]; then
  SERVICES=("$@")
else
  SERVICES=("${DEFAULT_SERVICES[@]}")
fi

echo "Building SmartCourier service images: ${SERVICES[*]}"
docker compose build "${SERVICES[@]}"

echo "Docker images built successfully."
