#!/bin/zsh

set -e

PROJECT_DIR="/Users/armansoni/Spring Project/CouriesDeleiveryManagement"

cd "$PROJECT_DIR"

chmod +x scripts/stop-all-services.sh
chmod +x scripts/run-all-services.sh
chmod +x scripts/rebuild-services.sh

./scripts/stop-all-services.sh
sleep 3
if [ "${1:-}" = "--build" ]; then
  ./scripts/run-all-services.sh --build
else
  ./scripts/run-all-services.sh
fi
