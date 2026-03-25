#!/bin/zsh

set +e

PROJECT_DIR="/Users/armansoni/Spring Project/CouriesDeleiveryManagement"

cd "$PROJECT_DIR"

echo "Stopping SmartCourier containers..."
docker compose stop \
  api-gateway admin-service tracking-service delivery-service auth-service config-server eureka-server \
  rabbitmq mysql-auth mysql-delivery mysql-tracking mysql-admin

echo "All managed services have been stopped."
