# API Samples

## Signup

`POST /gateway/auth/signup`

```json
{
  "fullName": "Aman Sharma",
  "email": "aman@example.com",
  "password": "Password@123",
  "phoneNumber": "9876543210"
}
```

## Login

`POST /gateway/auth/login`

```json
{
  "email": "aman@example.com",
  "password": "Password@123"
}
```

## Create Delivery

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
  "pickupDate": "2026-03-25"
}
```

## Update Delivery Status

`PUT /gateway/deliveries/{id}/status`

```json
{
  "status": "IN_TRANSIT"
}
```

## Add Tracking Event

`POST /gateway/tracking/events`

```json
{
  "trackingNumber": "SC20260323120000",
  "status": "IN_TRANSIT",
  "location": "Delhi Hub",
  "description": "Parcel departed from Delhi sorting facility"
}
```

## Save Delivery Proof

`PUT /gateway/tracking/proof`

```json
{
  "trackingNumber": "SC20260323120000",
  "recipientName": "Priya Singh",
  "proofNote": "Delivered to recipient",
  "proofImagePath": "uploads/tracking/proof-sample.jpg"
}
```

## Create Delivery Exception

`POST /gateway/admin/deliveries`

```json
{
  "deliveryId": 1,
  "trackingNumber": "SC20260323120000",
  "exceptionStatus": "DELAYED",
  "issueDescription": "Weather disruption at hub"
}
```

## Resolve Exception

`PUT /gateway/admin/deliveries/{id}/resolve`

```json
{
  "resolvedBy": "System Admin"
}
```

