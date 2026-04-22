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

### Prerequisites
- Java 17+
- Maven 3.6+

### Build & Run (Maven CLI)

```bash
# 1. Clone the repository
git clone <your-repo-url>
cd smart-campus-api

# 2. Build the project
mvn clean package

# 3. Start the server
mvn exec:java -Dexec.mainClass="com.smartcampus.Main"
```

The server will start at: http://localhost:8080/api/v1

### Alternative (NetBeans)
1. Open the project in NetBeans
2. Right-click → Clean and Build
3. Run the `Main` class

---

## Sample curl Commands

### 1. Get API Discovery Info
```bash
curl http://localhost:8080/api/v1
```

### 2. Create a Room
```bash
curl -X POST http://localhost:8080/api/v1/rooms \
-H "Content-Type: application/json" \
-d '{"id":"ROOM-1","name":"Library","capacity":50}'
```

### 3. Get All Rooms
```bash
curl http://localhost:8080/api/v1/rooms
```

### 4. Create a Sensor (linked to ROOM-1)
```bash
curl -X POST http://localhost:8080/api/v1/sensors \
-H "Content-Type: application/json" \
-d '{"id":"S1","type":"Temperature","status":"ACTIVE","currentValue":22.5,"roomId":"ROOM-1"}'
```

### 5. Filter Sensors by Type
```bash
curl "http://localhost:8080/api/v1/sensors?type=Temperature"
```

### 6. Add a Reading (auto-generates ID and timestamp)
```bash
curl -X POST http://localhost:8080/api/v1/sensors/S1/readings \
-H "Content-Type: application/json" \
-d '{"value":25.5}'
```

### 7. Get All Readings for a Sensor
```bash
curl http://localhost:8080/api/v1/sensors/S1/readings
```

### 8. Try to Delete Room with Sensors → 409 Conflict
```bash
curl -X DELETE http://localhost:8080/api/v1/rooms/ROOM-1
```

### 9. Create Sensor with Invalid roomId → 422 Unprocessable Entity
```bash
curl -X POST http://localhost:8080/api/v1/sensors \
-H "Content-Type: application/json" \
-d '{"id":"S2","type":"CO2","status":"ACTIVE","currentValue":0,"roomId":"FAKE-ROOM"}'
```

### 10. Post Reading to MAINTENANCE Sensor → 403 Forbidden
```bash
# First create a maintenance sensor
curl -X POST http://localhost:8080/api/v1/sensors \
-H "Content-Type: application/json" \
-d '{"id":"S-MAINT","type":"CO2","status":"MAINTENANCE","currentValue":0,"roomId":"ROOM-1"}'

# Then try to post a reading
curl -X POST http://localhost:8080/api/v1/sensors/S-MAINT/readings \
-H "Content-Type: application/json" \
-d '{"value":500}'
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
