#!/bin/zsh

set -e

export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH="$JAVA_HOME/bin:$PATH"

mkdir -p logs

echo "Starting config-server..."
nohup mvn -pl config-server spring-boot:run > logs/config-server.log 2>&1 &
sleep 8

echo "Starting auth-service..."
nohup mvn -pl auth-service spring-boot:run > logs/auth-service.log 2>&1 &

echo "Starting delivery-service..."
nohup mvn -pl delivery-service spring-boot:run > logs/delivery-service.log 2>&1 &

echo "Starting tracking-service..."
nohup mvn -pl tracking-service spring-boot:run > logs/tracking-service.log 2>&1 &

echo "Starting admin-service..."
nohup mvn -pl admin-service spring-boot:run > logs/admin-service.log 2>&1 &

sleep 8

echo "Starting api-gateway..."
nohup mvn -pl api-gateway spring-boot:run > logs/api-gateway.log 2>&1 &

echo "All start commands issued. Check logs/ for progress."

