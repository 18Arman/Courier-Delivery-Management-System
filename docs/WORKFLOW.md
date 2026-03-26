# SmartCourier Working Guide

## 1. Preferred startup flow

This project now uses a Docker-first workflow.

### Build service images

```bash
cd "/Users/armansoni/Spring Project/CouriesDeleiveryManagement"
chmod +x scripts/*.sh
./scripts/build-service-images.sh
```

### Start the full platform

```bash
./scripts/run-all-services.sh
```

### Stop the platform

```bash
./scripts/stop-all-services.sh
```

### Restart the platform

```bash
./scripts/restart-all-services.sh
```

## 2. Startup verification

After startup, check these URLs:

- Gateway Swagger: `http://localhost:8080/swagger-ui.html`
- Auth Swagger: `http://localhost:8081/swagger-ui.html`
- Delivery Swagger: `http://localhost:8082/swagger-ui.html`
- Tracking Swagger: `http://localhost:8083/swagger-ui.html`
- Admin Swagger: `http://localhost:8084/swagger-ui.html`
- Eureka Dashboard: `http://localhost:8761`
- Config Server: `http://localhost:8888`
- RabbitMQ UI: `http://localhost:15672`
- SonarQube UI: `http://localhost:9000`

## 3. Business flow

1. Customer signs up and logs in to get a JWT
2. Customer creates a delivery request
3. Delivery service stores the booking and generates a tracking number
4. Admin can fetch live delivery overview through Feign-based synchronous communication
5. Admin updates the delivery status
6. Delivery service publishes a lifecycle event to RabbitMQ
7. Tracking service consumes the event and adds tracking timeline entries
8. Admin service consumes exception-related events and creates exception cases
9. Admin service serves dashboard and repeated reads through cache-backed operations
10. Customer tracks delivery, uploads parcel documents, and later sees proof of delivery
11. Admin resolves exceptions and generates reports

## 4. Communication model

### Synchronous

- `admin-service` calls `delivery-service` through OpenFeign
- service discovery and resolution happen through Eureka and Spring Cloud LoadBalancer
- circuit breaker fallback keeps admin endpoints responsive if delivery-service is unavailable

### Asynchronous

- `delivery-service` publishes lifecycle events through RabbitMQ
- `tracking-service` consumes them to update tracking history
- `admin-service` consumes exception-related events to create or update operational exception records

### Cached reads

- `admin-service` uses Caffeine-backed caches for repeated dashboard and overview style reads
- writes and state-changing operations are expected to invalidate or refresh those views as configured in code

## 5. Docker networking model

Inside Docker, services talk to each other by service name, not `localhost`.

Examples:

- `config-server`
- `eureka-server`
- `rabbitmq`
- `mysql-auth`
- `mysql-delivery`
- `mysql-tracking`
- `mysql-admin`

`localhost` is only for your browser, Swagger, Postman, and host-machine curl commands.

## 6. Config modes

### Local config-repo mode

The current runtime reads externalized config from local files in `config-repo/`.

### Future GitHub config repo mode

The same files can later be moved to a GitHub-backed configuration repository and used by Config Server without changing the microservices themselves.

## 7. Runtime defaults

- default admin email: `admin@smartcourier.com`
- default admin password: `Admin@123`
- RabbitMQ username/password: `guest` / `guest@123`
- MySQL username/password: `root` / `root`
- allowed CORS origins are configured through shared config, not wildcard `*`

## 8. Manual local Maven mode

Manual startup is still available for development, but it is the secondary option now.

Before Maven commands, force Java 17:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH="$JAVA_HOME/bin:$PATH"
```

Typical order:

```bash
mvn -pl eureka-server spring-boot:run
```

```bash
mvn -pl config-server spring-boot:run
```

```bash
mvn -pl auth-service spring-boot:run
```

```bash
mvn -pl delivery-service spring-boot:run
```

```bash
mvn -pl tracking-service spring-boot:run
```

```bash
mvn -pl admin-service spring-boot:run
```

```bash
mvn -pl api-gateway spring-boot:run
```

When you use Maven mode with Docker databases, remember that the host machine sees mapped MySQL ports like `3307` to `3310`, not the internal container port names used inside Docker Compose.

## 9. Testing and quality

Run all tests:

```bash
mvn test
```

Run coverage:

```bash
mvn -Pcoverage verify
```

Run SonarQube analysis:

```bash
docker compose up -d sonarqube-db sonarqube
mvn -Pcoverage verify sonar:sonar -Dsonar.login=YOUR_SONAR_TOKEN
```

Use Maven output to confirm whether tests passed. Use SonarQube to inspect coverage and code quality metrics.
