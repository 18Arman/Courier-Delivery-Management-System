#!/bin/zsh

set -e

PROJECT_DIR="/Users/armansoni/Spring Project/CouriesDeleiveryManagement"

cd "$PROJECT_DIR"

echo "Building and starting SmartCourier containers..."
docker compose up --build -d \
  mysql-auth mysql-delivery mysql-tracking mysql-admin \
  rabbitmq eureka-server config-server auth-service delivery-service tracking-service admin-service api-gateway

echo
echo "All containers started in background."
echo "Gateway Swagger: http://localhost:8080/swagger-ui.html"
echo "Auth Swagger: http://localhost:8081/swagger-ui.html"
echo "Delivery Swagger: http://localhost:8082/swagger-ui.html"
echo "Tracking Swagger: http://localhost:8083/swagger-ui.html"
echo "Admin Swagger: http://localhost:8084/swagger-ui.html"
echo "Config Server: http://localhost:8888"
echo "Eureka Dashboard: http://localhost:8761"
echo "RabbitMQ UI: http://localhost:15672"
echo
echo "Use 'docker compose logs -f <service-name>' to inspect container logs."
