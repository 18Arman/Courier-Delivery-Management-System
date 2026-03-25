#!/bin/zsh

set -e

PROJECT_DIR="/Users/armansoni/Spring Project/CouriesDeleiveryManagement"

cd "$PROJECT_DIR"

chmod +x scripts/stop-all-services.sh
chmod +x scripts/run-all-services.sh

./scripts/stop-all-services.sh
sleep 3
./scripts/run-all-services.sh
