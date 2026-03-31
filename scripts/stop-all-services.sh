#!/bin/zsh

set +e

PROJECT_DIR="/Users/armansoni/Spring Project/CouriesDeleiveryManagement"

cd "$PROJECT_DIR"

echo "Stopping SmartCourier containers..."
docker compose stop \
  api-gateway notification-service admin-service tracking-service delivery-service auth-service config-server eureka-server \
  zipkin rabbitmq mysql-auth mysql-delivery mysql-tracking mysql-admin

echo "All managed services have been stopped."
