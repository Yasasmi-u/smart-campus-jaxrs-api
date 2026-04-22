# Smart Campus API

## Overview

This project is a RESTful API developed using **JAX-RS (Jersey)** and **Grizzly HTTP Server** as part of the Client-Server Architectures coursework.

The API manages:

* Rooms
* Sensors
* Sensor Readings

All data is stored using in-memory data structures such as **HashMap** and **ArrayList** (as required).

---

## How to Run the Project

1. Open the project in NetBeans
2. Clean and Build the project
3. Run the `Main` class

The server will start at:

http://localhost:8080/api/v1

---

## API Endpoints

### Rooms

* GET `/api/v1/rooms`
* POST `/api/v1/rooms`
* GET `/api/v1/rooms/{id}`
* DELETE `/api/v1/rooms/{id}`

### Sensors

* GET `/api/v1/sensors`
* POST `/api/v1/sensors`
* GET `/api/v1/sensors?type=Temperature`

### Sensor Readings

* GET `/api/v1/sensors/{id}/readings`
* POST `/api/v1/sensors/{id}/readings`

---

## Sample curl Commands

### 1. Get API Info

```bash
curl http://localhost:8080/api/v1
```

### 2. Create Room

```bash
curl -X POST http://localhost:8080/api/v1/rooms \
-H "Content-Type: application/json" \
-d '{"id":"ROOM-1","name":"Library","capacity":50}'
```

### 3. Create Sensor

```bash
curl -X POST http://localhost:8080/api/v1/sensors \
-H "Content-Type: application/json" \
-d '{"id":"S1","type":"Temperature","status":"ACTIVE","currentValue":22.5,"roomId":"ROOM-1"}'
```

### 4. Add Reading

```bash
curl -X POST http://localhost:8080/api/v1/sensors/S1/readings \
-H "Content-Type: application/json" \
-d '{"id":"R1","timestamp":1713594600000,"value":25.5}'
```

### 5. Filter Sensors

```bash
curl http://localhost:8080/api/v1/sensors?type=Temperature
```

---

## Error Handling

The API uses custom exception handling with appropriate HTTP status codes:

* **409 Conflict** → Room has active sensors
* **422 Unprocessable Entity** → Invalid roomId
* **403 Forbidden** → Sensor under maintenance
* **500 Internal Server Error** → Unexpected errors

All errors return structured JSON responses.

---

## Logging

A logging filter is implemented using JAX-RS filters.

It logs:

* Incoming request method and URL
* Outgoing response status

---

## Technologies Used

* Java
* JAX-RS (Jersey)
* Grizzly HTTP Server
* Maven

---

## Notes

* No database is used (as required)
* Data is stored in memory using HashMap and ArrayList
