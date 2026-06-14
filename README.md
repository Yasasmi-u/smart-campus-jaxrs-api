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

## Report: Answers to Coursework Questions

---

### Part 1.1 — JAX-RS Resource Class Lifecycle

By default, JAX-RS creates a new instance of a Resource class for every incoming HTTP request
(per-request lifecycle). This means each request gets its own object and no instance variables
are shared between requests.

This has a direct impact on how in-memory data must be managed. Because each resource instance
is thrown away after the request and you cannot store shared data (like rooms or sensors) as
instance-level fields as they would be lost after every single request. Instead, shared data
must live in a static, application-wide store such as a static class (like `DataStore` in
this project).

However, static data introduces a new problem: race conditions. If two requests arrive
simultaneously and both try to write to the same map, a plain `HashMap` can corrupt its internal
structure. To prevent it this project uses `ConcurrentHashMap` which is designed for safe
concurrent access without needing explicit `synchronized` blocks. For the readings list,
`putIfAbsent` is used to atomically initialise a list for a new sensor avoiding duplicate
initialisation across threads.

---

### Part 1.2 — Why HATEOAS Matters

HATEOAS (Hypermedia as the Engine of Application State) means that API responses include links
to related resources and available actions rather than just returning raw data.

This benefits client developers in several important ways. First, it reduces tight coupling
a client does not need to hardcode URLs or memorise the API structure because the server tells
the client what it can do next and where to go. Second, it makes the API self-documenting and
explorable; a developer can start at the root discovery endpoint and navigate the entire API
just by following the links provided in responses. Third, it future proofs the API: if a URL
changes the server updates its links and clients automatically follow the new paths without
breaking. In contrast, static documentation quickly becomes outdated leading to broken
integrations. The discovery endpoint in this project (`GET /api/v1`) demonstrates this principle
by providing a map of all primary resource paths.

---

### Part 2.1 — Returning Only IDs vs Full Room Objects

When a client calls `GET /api/v1/rooms`, there are two design choices: return only a list of
room IDs or return the full room objects.

Returning only IDs uses less bandwidth per response but forces the client to make N
additional requests (one per room) to fetch the actual data known as the N+1 problem. This
significantly increases latency and server load when there are many rooms.

Returning full objects costs more bandwidth in a single response but gives the client
everything it needs in one round trip which is faster and simpler. For this campus API rooms
contain relatively small payloads (id, name, capacity, sensorIds) so returning full objects is
the better choice. The trade-off only shifts in favour of IDs-only if individual objects were
very large (e.g., contained embedded binary data or deeply nested trees) which is not the case
here.

---

### Part 2.2 — Is DELETE Idempotent?

Yes, the DELETE operation is idempotent in this implementation but with an important
nuance.

Idempotency means that sending the same request multiple times produces same server state as
sending it once. If a client sends `DELETE /api/v1/rooms/ROOM-1` and room exists it is
deleted. If the same request is sent again the room no longer exists and server returns
`404 Not Found`. The server state after both calls is identical room is gone so 
operation is idempotent in terms of state.

However HTTP response code differs between first call (200 OK) and subsequent calls
(404 Not Found). This is acceptable and expected behaviour under REST. HTTP specification
does not require idempotent operations to return same response code every time only that
server side state remains consistent. Clients should therefore be prepared to handle a 404
on repeated DELETE calls without treating it as a critical error.

---

### Part 3.1 — Consequences of Sending Wrong Content-Type to @Consumes(APPLICATION_JSON)

The `@Consumes(MediaType.APPLICATION_JSON)` annotation tells JAX-RS runtime that POST
endpoint only accepts requests with a `Content-Type: application/json` header.

If a client sends data with a different content type for example `text/plain` or
`application/xml` JAX-RS framework intercepts request before it even reaches
method body and immediately returns `415 Unsupported Media Type`. The resource method is never
invoked. This is handled automatically by framework and requires no manual checking in code.

This is a useful contract: it guarantees method always receives a properly deserialised Java
object and it prevents malformed or unexpected data formats from silently entering
application logic.

---

### Part 3.2 — @QueryParam vs Path-Based Filtering

There are two ways to filter a collection: query parameters (`GET /sensors?type=CO2`) or path
segments (`GET /sensors/type/CO2`).

Query parameters are superior choice for filtering because they are optional by nature.
The same endpoint `GET /sensors` returns all sensors when no parameter is given and filtered
results when `?type=CO2` is appended. With a path based design, you would need two separate
routes one for all sensors and one for filtered sensors which duplicates logic and makes
API harder to maintain.

Additionally, query parameters are semantically correct: they describe how to search or filter
an existing resource collection not a different resource itself. Path segments should represent
resource identity (e.g., `/sensors/TEMP-001` identifies a specific sensor) not search
criteria. Using path segments for filtering also breaks RESTful conventions and can confuse
routing when combined with other path parameters.

---

### Part 4.1 — Benefits of the Sub-Resource Locator Pattern

The Sub-Resource Locator pattern allows a parent resource to delegate handling of a nested path
to a separate dedicated class. In this project, `SensorResource` delegates
`/{sensorId}/readings` to `SensorReadingResource`.

The main benefit is separation of concerns. Each class has a single, well defined
responsibility `SensorResource` manages sensors, `SensorReadingResource` manages readings.
If all paths were defined in one large controller class, file would grow very quickly and
become difficult to read, test and maintain.

It also improves testability because each sub-resource class can be unit-tested in
isolation without needing to instantiate the full parent resource. In large APIs with deep
nesting (e.g., buildings → floors → rooms → sensors → readings) this pattern keeps the
codebase modular and scalable. Finally, it mirrors the physical hierarchy of the domain,
making the code structure intuitive for any developer joining the project.

---

### Part 5.2 — Why HTTP 422 is More Accurate Than 404 for a Missing roomId Reference

A `404 Not Found` response typically means the requested URL/resource does not exist on the
server. If a client sends `POST /api/v1/sensors` with a valid URL, returning 404 would be
misleading the `/sensors` endpoint clearly does exist.

HTTP `422 Unprocessable Entity` is more semantically accurate because it signals that the
request was received and understood but it could not be processed due to a logical or
validation error in the payload. In this case, the request body is syntactically valid JSON but
contains a `roomId` that references a room that does not exist in the system. The problem is
not the URL, it is the content of the body.

Using 422 gives the client a clear signal that they need to fix the data in their request (use a
valid `roomId`), not the URL they are calling. This distinction makes error handling more
precise and debugging faster for API consumers.

---

### Part 5.4 — Cybersecurity Risks of Exposing Java Stack Traces

Exposing raw Java stack traces in API responses is a significant security risk for several
reasons.

First, stack traces reveal the internal package structure and class names of the application
(e.g., `com.smartcampus.resource.SensorResource.createSensor`). An attacker can use this to
understand exactly which frameworks, libraries and versions are in use then look up known
CVEs (vulnerabilities) for those specific versions.

Second, they expose file paths and line numbers which tells an attacker precisely where
logic is implemented and can assist in crafting targeted injection or bypass attacks.

Third, exception messages sometimes include sensitive data such as database queries, internal
IDs or configuration values that should never leave the server.

Finally, detailed error information enables reconnaissance even without a direct
vulnerability understanding the application's internals helps an attacker plan more effective
attacks. The global `ExceptionMapper<Throwable>` in this project prevents all of this by
catching every unhandled exception and returning only a generic `500 Internal Server Error`
message keeping all internal details server side.

---

## Notes

* No database is used (as required)
* Data is stored in memory using HashMap and ArrayList
