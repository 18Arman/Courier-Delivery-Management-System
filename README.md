# SmartCourier Delivery Management System

Backend-only microservices project for a courier and parcel delivery platform built with Spring Boot, Spring Cloud Gateway, Spring Cloud Config, MySQL, Spring Security JWT, OpenAPI/Swagger, JUnit, Mockito, JaCoCo, and SonarQube.

## What this project implements

- `config-server`: centralized configuration service that can read YAML files from a local folder now and from your GitHub config repo later
- `api-gateway`: single entry point that exposes the `/gateway/*` routes
- `auth-service`: customer signup/login, JWT generation, role management, and seeded admin account
- `delivery-service`: parcel booking, courier charge calculation, pickup scheduling, and delivery lifecycle management
- `tracking-service`: tracking timeline, document upload, and delivery proof storage
- `admin-service`: dashboard data, hub management, managed users, exception handling, and report generation

## Architecture used

This project follows a clean layered Spring Boot microservice structure inside each service:

`controller -> service -> repository -> database`

Each service keeps its own domain model and datasource so the services are independently deployable and easier to scale.

Package structure per service:

- `config`: Spring Security, bootstrap seeders, or service-specific configuration
- `controller`: REST API layer
- `dto`: request/response contracts
- `entity`: JPA models
- `repository`: Spring Data JPA persistence layer
- `service`: business rules
- `exception`: centralized error handling
- `security`: JWT parsing and authentication filter

## Why this architecture is good for Spring Boot microservices

- Separation of concerns: controllers stay thin, services hold business logic, repositories only handle persistence
- Easy maintenance: each bounded context is isolated by service
- Independent databases: better ownership and easier production scaling
- Config externalization: `application.yml` values can move to a central GitHub config repository
- API-first development: Swagger/OpenAPI is enabled in every service
- Security-first design: JWT and role-based authorization are built in from the beginning

## Services and default ports

- Gateway: `8080`
- Auth: `8081`
- Delivery: `8082`
- Tracking: `8083`
- Admin: `8084`
- Config Server: `8888`

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
- Roles: `ROLE_CUSTOMER`, `ROLE_ADMIN`
- Method-level authorization using `@PreAuthorize`
- Swagger is still accessible for testing
- Gateway acts as the public entry point, while services validate JWT tokens

## Swagger URLs

Start each service and open:

- `http://localhost:8081/swagger-ui.html`
- `http://localhost:8082/swagger-ui.html`
- `http://localhost:8083/swagger-ui.html`
- `http://localhost:8084/swagger-ui.html`
- `http://localhost:8080/swagger-ui.html`
- `http://localhost:8888/swagger-ui.html`

## MySQL design

Each microservice uses its own schema:

- `smartcourier_auth`
- `smartcourier_delivery`
- `smartcourier_tracking`
- `smartcourier_admin`

This is the recommended microservices pattern because it avoids tight coupling between services at database level.

## Configuration management through GitHub repo

You asked for centralized config via a separate GitHub repo. The project is prepared for that.

Files already created for you:

- [`config-repo/application.yml`](/Users/armansoni/Spring Project/CouriesDeleiveryManagement/config-repo/application.yml)
- [`config-repo/api-gateway.yml`](/Users/armansoni/Spring Project/CouriesDeleiveryManagement/config-repo/api-gateway.yml)
- [`config-repo/auth-service.yml`](/Users/armansoni/Spring Project/CouriesDeleiveryManagement/config-repo/auth-service.yml)
- [`config-repo/delivery-service.yml`](/Users/armansoni/Spring Project/CouriesDeleiveryManagement/config-repo/delivery-service.yml)
- [`config-repo/tracking-service.yml`](/Users/armansoni/Spring Project/CouriesDeleiveryManagement/config-repo/tracking-service.yml)
- [`config-repo/admin-service.yml`](/Users/armansoni/Spring Project/CouriesDeleiveryManagement/config-repo/admin-service.yml)
- [`config-repo/config-server.yml`](/Users/armansoni/Spring Project/CouriesDeleiveryManagement/config-repo/config-server.yml)

For now, `config-server` reads from the local `config-repo/` folder. Later, you can move the same files into your GitHub config repository and point Spring Cloud Config Server to that repo URL.

## Important note about Java 17 on your laptop

Your project targets Java 17 and was validated with Java 17. Maven on this machine is currently defaulting to Java 25, so before running the project you should point Maven to Java 17:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH="$JAVA_HOME/bin:$PATH"
```

Then verify:

```bash
java -version
mvn -version
```

Both should show Java 17 during project build and run.

## Run order

### 1. Start MySQL

You can use local MySQL or Docker. A ready compose file is included:

- [`docker-compose.yml`](/Users/armansoni/Spring Project/CouriesDeleiveryManagement/docker-compose.yml)

If you use Docker Compose:

```bash
docker compose up -d mysql-auth mysql-delivery mysql-tracking mysql-admin
```

### 2. Start Config Server

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl config-server spring-boot:run
```

### 3. Start the other services

Open new terminals and run:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl api-gateway spring-boot:run
```

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl auth-service spring-boot:run
```

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl delivery-service spring-boot:run
```

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl tracking-service spring-boot:run
```

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -pl admin-service spring-boot:run
```

## Default admin user

The auth service seeds an admin user automatically on first run:

- Email: `admin@smartcourier.com`
- Password: `Admin@123`

You can change these values in [`auth-service/src/main/resources/application.yml`](/Users/armansoni/Spring Project/CouriesDeleiveryManagement/auth-service/src/main/resources/application.yml) or in your external config repo.

## Testing, JaCoCo, and SonarQube

Unit tests were added for the main service layer classes. JaCoCo is configured in the Maven build and Sonar properties are included.

Run tests:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH="$JAVA_HOME/bin:$PATH"
mvn test
```

Generate aggregated coverage:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -Pcoverage verify
```

Start SonarQube with Docker:

```bash
docker compose up -d sonarqube-db sonarqube
```

Then analyze:

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH="$JAVA_HOME/bin:$PATH"
mvn -Pcoverage verify sonar:sonar
```

## Concepts explained simply

### 1. Why Config Server

Instead of hardcoding database URLs and secrets inside every microservice, Config Server centralizes those values. In a real company setup, that config usually lives in a dedicated GitHub repo.

### 2. Why API Gateway

Clients should not call many microservices directly. The gateway gives one public URL and forwards traffic internally to the correct service.

### 3. Why one database per service

This is an important microservices best practice. If every service writes to the same schema, services become tightly coupled and harder to maintain.

### 4. Why DTOs

DTOs stop your database entities from leaking directly into your API contract. That makes validation and future changes much safer.

### 5. Why service layer

Business logic such as charge calculation, exception resolution, or proof storage belongs in services, not in controllers.

### 6. Why Swagger

Because you are testing through Postman and Swagger rather than Angular, Swagger gives you self-documenting APIs and a quick UI for manual testing.

### 7. Why JWT

JWT lets the backend stay stateless. After login, the token carries the user identity and roles, so later requests can be authorized without storing server-side sessions.

## Suggested next improvements

- Add inter-service communication with OpenFeign or event messaging for stronger admin aggregation
- Add refresh token support
- Add object storage integration for uploaded documents
- Add Testcontainers for real MySQL integration tests
- Add CI pipeline for Maven, JaCoCo, and SonarQube quality gate

