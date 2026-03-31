#!/bin/zsh

set -e

PROJECT_DIR="/Users/armansoni/Spring Project/CouriesDeleiveryManagement"
DEFAULT_SERVICES=(eureka-server config-server auth-service delivery-service tracking-service admin-service notification-service api-gateway)

cd "$PROJECT_DIR"

if [ "$#" -gt 0 ]; then
  SERVICES=("$@")
else
  SERVICES=("${DEFAULT_SERVICES[@]}")
fi

echo "Pulling SmartCourier service images from Docker Hub: ${SERVICES[*]}"
docker compose pull "${SERVICES[@]}"

echo "Docker images pulled successfully."
