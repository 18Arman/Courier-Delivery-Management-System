#!/bin/zsh

set -e

PROJECT_DIR="/Users/armansoni/Spring Project/CouriesDeleiveryManagement"
ALL_SERVICES=(
  mysql-auth mysql-delivery mysql-tracking mysql-admin
  rabbitmq zipkin eureka-server config-server auth-service delivery-service tracking-service admin-service api-gateway
)

cd "$PROJECT_DIR"

if [ "${1:-}" = "--build" ]; then
  echo "Building and starting SmartCourier containers..."
  docker compose up --build -d "${ALL_SERVICES[@]}"
else
  echo "Starting SmartCourier containers..."
  docker compose up -d "${ALL_SERVICES[@]}"
fi

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
echo "Zipkin UI: http://localhost:9411"
echo
echo "Tip: use './scripts/run-all-services.sh --build' only after code changes that need fresh images."
echo "Tip: use './scripts/build-service-images.sh <service-name>' and 'docker compose up -d <service-name>' to update one service."
echo "Use 'docker compose logs -f <service-name>' to inspect container logs."
