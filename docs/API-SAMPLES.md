# API Samples

These samples assume you are calling the system through the API Gateway.

## Auth context

- Customer token required:
  - `POST /gateway/deliveries`
  - `GET /gateway/deliveries/my`
  - `GET /gateway/tracking/{trackingNumber}`
  - `POST /gateway/tracking/documents/upload`
  - `GET /gateway/tracking/{trackingNumber}/proof`

- Admin token required:
  - `PUT /gateway/deliveries/{id}/status`
  - `GET /gateway/admin/dashboard`
  - `GET /gateway/admin/deliveries/{deliveryId}/overview`
  - `GET /gateway/admin/deliveries`
  - `PUT /gateway/admin/deliveries/{id}/resolve`
  - `POST /gateway/admin/reports`

Use:

```http
Authorization: Bearer <jwt-token>
```

## Customer signup

`POST /gateway/auth/signup`

```json
{
  "fullName": "Aman Sharma",
  "email": "aman@example.com",
  "password": "Password@123",
  "phoneNumber": "9876543210"
}
```

## Customer login

`POST /gateway/auth/login`

```json
{
  "email": "aman@example.com",
  "password": "Password@123"
}
```

## Admin login

`POST /gateway/auth/login`

```json
{
  "email": "admin@smartcourier.com",
  "password": "Admin@123"
}
```

## Create delivery

`POST /gateway/deliveries`

```json
{
  "serviceType": "EXPRESS",
  "sender": {
    "contactName": "Aman Sharma",
    "phoneNumber": "9876543210",
    "line1": "221 Green Street",
    "city": "Delhi",
    "state": "Delhi",
    "postalCode": "110001",
    "country": "India"
  },
  "receiver": {
    "contactName": "Priya Singh",
    "phoneNumber": "9988776655",
    "line1": "17 Lake View Road",
    "city": "Mumbai",
    "state": "Maharashtra",
    "postalCode": "400001",
    "country": "India"
  },
  "parcel": {
    "parcelType": "Documents",
    "weightInKg": 1.5,
    "declaredValue": 2500,
    "dimensions": "12x8x2 cm",
    "notes": "Handle carefully"
  },
  "pickupDate": "2026-03-28"
}
```

## Fetch customer deliveries

`GET /gateway/deliveries/my`

No request body.

## Fetch admin delivery overview

`GET /gateway/admin/deliveries/{deliveryId}/overview`

Example:

```text
GET /gateway/admin/deliveries/1/overview
```

This endpoint is served by `admin-service`, which fetches live delivery data from `delivery-service` through OpenFeign and circuit breaker protection.

## Update delivery status to trigger async event

`PUT /gateway/deliveries/{id}/status`

```json
{
  "status": "IN_TRANSIT"
}
```

This request is synchronous, but it also publishes a RabbitMQ event for downstream consumers.

## Track delivery after async update

`GET /gateway/tracking/{trackingNumber}`

Example:

```text
GET /gateway/tracking/SC20260324141510
```

The tracking timeline may reflect event-driven updates after message consumption, not necessarily in the same HTTP response that updated the delivery.

## Save delivery proof

`PUT /gateway/tracking/proof`

```json
{
  "trackingNumber": "SC20260324141510",
  "recipientName": "Priya Singh",
  "proofNote": "Delivered to recipient after identity check",
  "proofImagePath": "uploads/tracking/proof-sample.jpg"
}
```

## Create delivery exception manually

`POST /gateway/admin/deliveries`

```json
{
  "deliveryId": 1,
  "trackingNumber": "SC20260324141510",
  "exceptionStatus": "DELAYED",
  "issueDescription": "Weather disruption at hub"
}
```

In normal flow, many exception records are created asynchronously from delivery lifecycle events.

## Resolve exception

`PUT /gateway/admin/deliveries/{id}/resolve`

```json
{
  "resolvedBy": "System Admin"
}
```

## Generate report

`POST /gateway/admin/reports`

No request body.

## Event-driven note

Some system behaviors are asynchronous:

- delivery status updates publish events
- tracking timeline entries may appear after RabbitMQ consumption
- admin exception records may be created after event processing

So when testing end-to-end behavior, allow a short delay before verifying tracking or exception outcomes.
