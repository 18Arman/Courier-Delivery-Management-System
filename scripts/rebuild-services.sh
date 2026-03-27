#!/bin/zsh

set -e

PROJECT_DIR="/Users/armansoni/Spring Project/CouriesDeleiveryManagement"

cd "$PROJECT_DIR"

if [ "$#" -eq 0 ]; then
  echo "Usage: ./scripts/rebuild-services.sh <service-name> [more-service-names]"
  echo "Example: ./scripts/rebuild-services.sh api-gateway config-server"
  exit 1
fi

echo "Rebuilding and restarting selected services: $*"
docker compose up --build -d "$@"

echo "Selected services rebuilt and started."
