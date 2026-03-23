# SmartCourier Working Guide

## 1. Set Java 17

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH="$JAVA_HOME/bin:$PATH"
```

## 2. Start databases

```bash
docker compose up -d mysql-auth mysql-delivery mysql-tracking mysql-admin
```

## 3. Start services

You can either run each service manually or use the helper script:

```bash
./scripts/start-services.sh
```

Manual order:

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

## 4. Business flow

1. Customer signs up
2. Customer logs in and gets JWT
3. Customer creates delivery
4. Customer checks own deliveries
5. Admin logs in
6. Admin updates delivery status
7. Admin adds tracking event
8. Customer tracks delivery
9. Customer uploads parcel document
10. Admin stores proof of delivery
11. Admin resolves exceptions and generates reports

## 5. Swagger URLs

- http://localhost:8080/swagger-ui.html
- http://localhost:8081/swagger-ui.html
- http://localhost:8082/swagger-ui.html
- http://localhost:8083/swagger-ui.html
- http://localhost:8084/swagger-ui.html

## 6. Default admin credentials

- Email: `admin@smartcourier.com`
- Password: `Admin@123`

