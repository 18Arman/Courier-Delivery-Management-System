# SmartCourier Delivery Management System

Backend-only courier and parcel delivery platform built with Spring Boot microservices, Spring Cloud Gateway, Spring Cloud Config, Eureka Server, OpenFeign, Spring Cloud LoadBalancer, RabbitMQ, MySQL, JWT security, OpenAPI/Swagger, JaCoCo, and SonarQube.

## What this project implements

- `eureka-server`: service discovery registry for runtime registration and discovery
- `config-server`: centralized configuration service backed by local `config-repo/` now and ready for a future GitHub config repo
- `api-gateway`: public entry point exposing `/gateway/*` routes with discovery-aware `lb://` routing
- `auth-service`: customer signup/login, JWT generation, role management, and seeded admin account
- `delivery-service`: parcel booking, courier charge calculation, pickup scheduling, lifecycle updates, and event publishing
- `tracking-service`: tracking timeline, document upload, and proof-of-delivery storage
- `admin-service`: dashboard, reports, hubs, managed users, exception handling, cached reads, and Feign-based live delivery aggregation
- synchronous service-to-service calls through OpenFeign with circuit breaker fallbacks
- asynchronous delivery events through RabbitMQ from delivery to tracking/admin
- Docker-first startup through helper scripts and `docker compose`

## Architecture used

Each service follows a layered Spring Boot structure:

`controller -> service -> repository -> database`

Shared platform architecture:

- `Eureka Server`: all runtime services register themselves for discovery
- `Spring Cloud Config`: services read shared config from `config-repo/`
- `API Gateway`: routes external requests to services through `lb://service-name`
- `OpenFeign + LoadBalancer`: `admin-service` fetches live delivery data by service name, not fixed hostnames
- `RabbitMQ`: `delivery-service` publishes lifecycle events, `tracking-service` and `admin-service` consume them
- `Caffeine cache`: `admin-service` caches dashboard and repeated read-heavy views
- `Resilience4j circuit breaker`: live admin reads degrade safely if `delivery-service` is unavailable

Package structure per service:

- `config`: Spring Security, cache, OpenAPI, or service-specific configuration
- `controller`: REST API layer
- `dto`: request/response contracts
- `entity`: JPA models
- `repository`: Spring Data JPA persistence layer
- `service`: business rules
- `exception`: centralized error handling
- `security`: JWT parsing and authentication filter
- `messaging` / `integration`: RabbitMQ listeners/publishers and interservice clients where applicable

## Why this architecture is good for Spring Boot microservices

- Separation of concerns: thin controllers, focused services, isolated repositories
- Independent data ownership: each business service owns its own MySQL schema
- Dynamic discovery: services and gateway resolve each other through Eureka
- Mixed communication styles: Feign for immediate reads, RabbitMQ for eventual consistency
- Resilience: circuit breaker fallbacks stop downstream failures from breaking admin reads
- Better read performance: cache-backed admin dashboards and lookups reduce repeated expensive calls
- Deployability: the full platform can be started as containers with one command

## Services and default ports

- API Gateway: `8080`
- Auth Service: `8081`
- Delivery Service: `8082`
- Tracking Service: `8083`
- Admin Service: `8084`
- Eureka Server: `8761`
- Config Server: `8888`
- RabbitMQ broker: `5672`
- RabbitMQ UI: `15672`
- SonarQube: `9000`

## Interservice communication

### Synchronous

- `admin-service` uses OpenFeign to call `delivery-service`
- delivery overview and dashboard delivery counts are fetched in request-response style
- circuit breaker fallback returns safe responses if `delivery-service` is unavailable

### Asynchronous

- `delivery-service` publishes lifecycle events to RabbitMQ
- `tracking-service` consumes those events and creates tracking history automatically
- `admin-service` consumes exception-related events and creates/updates exception cases

### Load balancing and discovery

- services register in Eureka at runtime
- gateway routes use `lb://AUTH-SERVICE`, `lb://DELIVERY-SERVICE`, and similar discovery-aware service names
- Feign clients also resolve instances through Spring Cloud LoadBalancer

### Caching

- `admin-service` uses Caffeine-backed caches for repeated read-heavy endpoints such as dashboard and delivery overview style reads
- cache keeps admin reads fast while preserving write-through invalidation on changes

## Docker deployment model

Docker is now the primary runtime model for this project.

- inside containers, services do **not** use `localhost` to reach each other
- they communicate using Docker service names such as `config-server`, `eureka-server`, `rabbitmq`, `mysql-auth`, and `delivery-service`
- host machine URLs like `http://localhost:8080` are only for your browser/Postman from outside Docker

## Docker-first run flow

### 1. Build service images

```bash
cd "/Users/armansoni/Spring Project/CouriesDeleiveryManagement"
chmod +x scripts/*.sh
./scripts/build-service-images.sh
```

### 2. Start the full platform

```bash
./scripts/run-all-services.sh
```

This starts:

- all four MySQL containers
- RabbitMQ
- Eureka Server
- Config Server
- Auth, Delivery, Tracking, and Admin services
- API Gateway

### 3. Stop everything

```bash
./scripts/stop-all-services.sh
```

### 4. Restart everything

```bash
./scripts/restart-all-services.sh
```

### Direct Docker Compose equivalents

```bash
docker compose build eureka-server config-server auth-service delivery-service tracking-service admin-service api-gateway
docker compose up --build -d
docker compose stop
docker compose logs -f api-gateway
```

## Runtime URLs

- Gateway Swagger: `http://localhost:8080/swagger-ui.html`
- Auth Swagger: `http://localhost:8081/swagger-ui.html`
- Delivery Swagger: `http://localhost:8082/swagger-ui.html`
- Tracking Swagger: `http://localhost:8083/swagger-ui.html`
- Admin Swagger: `http://localhost:8084/swagger-ui.html`
- Eureka Dashboard: `http://localhost:8761`
- Config Server: `http://localhost:8888`
- RabbitMQ UI: `http://localhost:15672`
- SonarQube UI: `http://localhost:9000`

## Implemented APIs

### Gateway-facing routes

- `GET /gateway/services`
- `POST /gateway/auth/signup`
- `POST /gateway/auth/login`
- `GET /gateway/auth/me`
- `POST /gateway/deliveries`
- `GET /gateway/deliveries/my`
- `GET /gateway/deliveries/{id}`
- `PUT /gateway/deliveries/{id}/status`
- `GET /gateway/tracking/{trackingNumber}`
- `POST /gateway/tracking/events`
- `POST /gateway/tracking/documents/upload`
- `PUT /gateway/tracking/proof`
- `GET /gateway/tracking/{trackingNumber}/proof`
- `GET /gateway/admin/dashboard`
- `GET /gateway/admin/deliveries`
- `GET /gateway/admin/deliveries/{deliveryId}/overview`
- `POST /gateway/admin/deliveries`
- `PUT /gateway/admin/deliveries/{id}/resolve`
- `GET /gateway/admin/reports`
- `POST /gateway/admin/reports`
- `GET /gateway/admin/users`
- `POST /gateway/admin/users`
- `GET /gateway/admin/hubs`
- `POST /gateway/admin/hubs`

## Delivery lifecycle covered

- `DRAFT`
- `BOOKED`
- `PICKED_UP`
- `IN_TRANSIT`
- `OUT_FOR_DELIVERY`
- `DELIVERED`
- `DELAYED`
- `FAILED`
- `RETURNED`

## Security design

- JWT-based stateless authentication
- roles: `ROLE_CUSTOMER`, `ROLE_ADMIN`
- method-level authorization using `@PreAuthorize`
- each service validates JWTs independently
- Swagger remains available for API testing
- CORS allowed origins are now configured through shared config, not permissive wildcard origins

## MySQL design

Each microservice uses its own schema:

- `smartcourier_auth`
- `smartcourier_delivery`
- `smartcourier_tracking`
- `smartcourier_admin`

Default Docker MySQL access:

- auth DB host in Docker: `mysql-auth`
- delivery DB host in Docker: `mysql-delivery`
- tracking DB host in Docker: `mysql-tracking`
- admin DB host in Docker: `mysql-admin`
- host machine mapped ports: `3307`, `3308`, `3309`, `3310`
- username/password defaults: `root` / `root`

## Message broker

RabbitMQ is used for asynchronous communication:

- broker port: `5672`
- management UI: `http://localhost:15672`
- default username/password: `guest` / `guest`

## Configuration management

This project supports two config modes:

### Local config repo mode

`config-server` reads from the local [`config-repo/application.yml`](/Users/armansoni/Spring%20Project/CouriesDeleiveryManagement/config-repo/application.yml) and per-service YAML files during local and Docker-based runs.

### Future GitHub config repo mode

The same YAML files can later be moved into a dedicated GitHub config repository and `config-server` can be pointed at that remote source without changing the services themselves.

Prepared config files:

- [config-repo/application.yml](/Users/armansoni/Spring%20Project/CouriesDeleiveryManagement/config-repo/application.yml)
- [config-repo/api-gateway.yml](/Users/armansoni/Spring%20Project/CouriesDeleiveryManagement/config-repo/api-gateway.yml)
- [config-repo/auth-service.yml](/Users/armansoni/Spring%20Project/CouriesDeleiveryManagement/config-repo/auth-service.yml)
- [config-repo/delivery-service.yml](/Users/armansoni/Spring%20Project/CouriesDeleiveryManagement/config-repo/delivery-service.yml)
- [config-repo/tracking-service.yml](/Users/armansoni/Spring%20Project/CouriesDeleiveryManagement/config-repo/tracking-service.yml)
- [config-repo/admin-service.yml](/Users/armansoni/Spring%20Project/CouriesDeleiveryManagement/config-repo/admin-service.yml)

Shared defaults currently documented there include:

- JWT secret and Swagger path
- allowed CORS origins
- service discovery properties
- cache names and expiration values where applicable

## Default credentials and runtime defaults

- seeded admin email: `admin@smartcourier.com`
- seeded admin password: `Admin@123`
- RabbitMQ user/password: `guest` / `guest`
- MySQL root/password: `root` / `root`
- default allowed CORS origins: `http://localhost:3000`, `http://localhost:4200`, `http://localhost:8080`

## Java 17 note for local development

Docker runtime does not require you to manually launch services with Java, but Maven-based local development and testing still require Java 17:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH="$JAVA_HOME/bin:$PATH"
java -version
mvn -version
```

Use Java 17 before:

- `mvn test`
- `mvn -Pcoverage verify`
- `mvn sonar:sonar`
- running services manually outside Docker

## Optional manual Maven mode

Manual startup is still supported for development, but it is now the secondary option.

Typical manual order:

```bash
mvn -pl eureka-server spring-boot:run
mvn -pl config-server spring-boot:run
mvn -pl auth-service spring-boot:run
mvn -pl delivery-service spring-boot:run
mvn -pl tracking-service spring-boot:run
mvn -pl admin-service spring-boot:run
mvn -pl api-gateway spring-boot:run
```

When using manual local mode with Docker databases, service DB host/port overrides may still be needed because the host machine sees mapped ports like `3307` instead of container-internal `3306`.

## Testing, JaCoCo, and SonarQube

### Test execution

Maven is the source of truth for whether tests passed.

Run all tests:

```bash
mvn test
```

Run one service only:

```bash
mvn -pl auth-service test
```

`BUILD SUCCESS` means the executed tests passed. `BUILD FAILURE` means at least one test failed.

### Coverage

Generate JaCoCo coverage:

```bash
mvn -Pcoverage verify
```

### SonarQube

Start SonarQube:

```bash
docker compose up -d sonarqube-db sonarqube
```

Run analysis:

```bash
mvn -Pcoverage verify sonar:sonar -Dsonar.login=YOUR_SONAR_TOKEN
```

Use SonarQube mainly for:

- coverage metrics
- code smells
- bugs and vulnerabilities
- quality gate status

Use Maven output to verify pass/fail of test cases.

## GitHub readiness

### What to commit

- source code
- `pom.xml` files
- `docker-compose.yml`
- Dockerfiles
- scripts
- `config-repo/` templates
- docs and README
- Postman collection

### What not to commit

- `target/`
- local logs
- IDE metadata like `.idea/`
- `.DS_Store`
- real secrets, tokens, or production credentials
- generated upload/test artifacts

## Project demo flow

Recommended demo sequence:

1. Start the platform with Docker scripts
2. Show Eureka registrations
3. Log in as admin and customer
4. Create a delivery
5. Show admin delivery overview through Feign
6. Update status and explain RabbitMQ event propagation
7. Show tracking timeline created by event consumption
8. Show exception capture and resolution in admin
9. Show proof/document flow
10. Show SonarQube and coverage reports

## Useful project files

- [docs/WORKFLOW.md](/Users/armansoni/Spring%20Project/CouriesDeleiveryManagement/docs/WORKFLOW.md)
- [docs/API-SAMPLES.md](/Users/armansoni/Spring%20Project/CouriesDeleiveryManagement/docs/API-SAMPLES.md)
- [docker-compose.yml](/Users/armansoni/Spring%20Project/CouriesDeleiveryManagement/docker-compose.yml)
- [scripts/build-service-images.sh](/Users/armansoni/Spring%20Project/CouriesDeleiveryManagement/scripts/build-service-images.sh)
- [scripts/run-all-services.sh](/Users/armansoni/Spring%20Project/CouriesDeleiveryManagement/scripts/run-all-services.sh)
- [scripts/stop-all-services.sh](/Users/armansoni/Spring%20Project/CouriesDeleiveryManagement/scripts/stop-all-services.sh)
- [scripts/restart-all-services.sh](/Users/armansoni/Spring%20Project/CouriesDeleiveryManagement/scripts/restart-all-services.sh)
