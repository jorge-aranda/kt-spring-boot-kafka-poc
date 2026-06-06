# Kafka POC — Kotlin + Spring Boot 4 + MongoDB

> A didactic event-driven microservice playground built with **Kotlin**, **Spring Boot 4.0.x**,
> **Spring for Apache Kafka** and **MongoDB**, structured around **hexagonal architecture**.

This repository is a **proof of concept** for learning Apache Kafka end-to-end: a `tasks` domain
publishes events to Kafka, and a `notifications` domain consumes them to materialize per-user
notifications. The whole stack is self-contained in Docker so you can run it without installing
Mongo, Kafka or Gradle on the host.

---

## Project Purpose

- Practice **event-driven design** with Kafka producers, consumers, topics and consumer groups.
- Apply **hexagonal architecture** (api / application / domain / infrastructure) to two real domains.
- Use **Spring Boot 4** (Spring Framework 7, Jakarta EE 11) with **Kotlin 2.x**.
- Persist domain state in **MongoDB** via Spring Data.
- Ship everything as a **non-root Docker image** orchestrated with Docker Compose.

This is intentionally a learning project, not a production service.

---

## Stack

| Concern         | Choice                                                |
|-----------------|-------------------------------------------------------|
| Language        | Kotlin 2.x                                            |
| Framework       | Spring Boot **4.0.x** on Spring Framework 7          |
| JVM             | Java **17**                                           |
| Build           | Gradle (Kotlin DSL) via the wrapper (`./gradlew`)    |
| Database        | MongoDB 7                                             |
| Messaging       | Apache Kafka 3.8 (KRaft mode, no Zookeeper)          |
| Containerization| Docker + Docker Compose                               |
| Base package    | `io.jaranas.kafkapoc`                                 |
| Gradle project  | `io.jaranas.kafka-poc`                                |

---

## Prerequisites

- **Java 17** (only needed if you want to build/run outside Docker).
- **Docker** and **Docker Compose** v2 (the recommended path).

No local MongoDB or Kafka installation is required — Docker Compose provides both.

---

## Running with Docker Compose (recommended)

From the repository root:

```bash
docker compose up --build
```

This starts three containers:

- `kafkapoc-mongodb` — MongoDB 7 on `localhost:27017`.
- `kafkapoc-kafka` — Apache Kafka 3.8 in KRaft mode. Internal listener `kafka:9092`,
  external listener exposed on the host at `localhost:9094`.
- `kafkapoc-app` — the Spring Boot application on `localhost:8080`.

Verify the app is up:

```bash
curl http://localhost:8080/actuator/health
# => {"groups":["liveness","readiness"],"status":"UP"}
```

To tear everything down (and remove the Mongo volume):

```bash
docker compose down -v
```

---

## Running Locally (without Docker)

You still need a MongoDB and a Kafka broker reachable from the host. With both running on
`localhost` you can use the defaults from `application.yml`:

```bash
./gradlew build
./gradlew bootRun
```

Run the tests (no Mongo / Kafka required — autoconfiguration is excluded in the smoke test):

```bash
./gradlew test
```

---

## Building the Docker Image Directly

```bash
docker build -t kafka-poc .
docker run --rm -p 8080:8080 \
  -e SPRING_MONGODB_URI=mongodb://host.docker.internal:27017/kafkapoc \
  -e SPRING_KAFKA_BOOTSTRAP_SERVERS=host.docker.internal:9094 \
  kafka-poc
```

The image runs as the non-root user `appuser`.

---

## Configuration

All connection settings are configurable via environment variables (Spring Boot relaxed binding):

| Environment variable             | Default (Docker)                    | Default (local)                      |
|----------------------------------|-------------------------------------|--------------------------------------|
| `SPRING_MONGODB_URI`             | `mongodb://mongodb:27017/kafkapoc`  | `mongodb://localhost:27017/kafkapoc` |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | `kafka:9092`                        | `localhost:9092`                     |
| `SPRING_KAFKA_CONSUMER_GROUP_ID` | `kafka-poc`                         | `kafka-poc`                          |
| `APP_SECURITY_DEV_USER_ID`       | _(not set)_                         | `00000000-0000-0000-0000-000000000001` |

### Authentication (local development)

All `/api/**` endpoints require HTTP Basic authentication. A single in-memory user is configured
for development:

- **Username:** the UUIDv4 set in `APP_SECURITY_DEV_USER_ID` (default: `00000000-0000-0000-0000-000000000001`)
- **Password:** `dev`

Example with `curl`:

```bash
curl -u 00000000-0000-0000-0000-000000000001:dev \
  -X PUT http://localhost:8080/api/tasks/$(uuidgen) \
  -H 'Content-Type: application/json' \
  -d '{"title": "My first task", "description": "Hello world"}'
```

Spring Boot 4 renamed the MongoDB properties: use `spring.mongodb.*`
(formerly `spring.data.mongodb.*`).

---

## Architecture

The application follows **hexagonal architecture**. Each domain lives under
`io.jaranas.kafkapoc.<domain>` with four layers:

```
api.controller / api.model
        │
application.usecase / application.model
        │
domain.model / domain.service / domain.repository / domain.event
        │
infrastructure.repository.impl / infrastructure.messaging.{producer,consumer,model}
```

For the full visibility rules, package layout and Kafka adapter conventions see
[`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md).

---

## Domains & Endpoints

### Tasks (`io.jaranas.kafkapoc.tasks`)

**Milestone 1 (implemented):** REST CRUD + MongoDB persistence. Kafka events publishing not implemented yet — coming in next milestone.

| Use case         | HTTP     | Endpoint                              |
|------------------|----------|---------------------------------------|
| Create task      | `PUT`    | `/api/tasks/{taskId}`                 |
| Get task         | `GET`    | `/api/tasks/{taskId}`                 |
| List tasks       | `GET`    | `/api/tasks`                          |
| Complete task    | `PATCH`  | `/api/tasks/{taskId}/complete`        |
| Archive task     | `DELETE` | `/api/tasks/{taskId}`                 |

### Notifications (`io.jaranas.kafkapoc.notifications`)

Kafka consumer that materializes notifications from `tasks` events. No write endpoints.

| Use case             | HTTP     | Endpoint                                       |
|----------------------|----------|------------------------------------------------|
| List notifications   | `GET`    | `/api/notifications`                           |
| Get notification     | `GET`    | `/api/notifications/{notificationId}`          |
| Mark as read         | `PATCH`  | `/api/notifications/{notificationId}/read`     |
| Archive notification | `DELETE` | `/api/notifications/{notificationId}`          |

Full domain registry and decisions: [`docs/DOMAINS.md`](docs/DOMAINS.md).

---

## MCP Server (Model Context Protocol)

The `tasks` domain use cases are also exposed through an **MCP server** as an alternative
entry point to the REST API. This lets MCP-compatible clients (Claude Desktop, IDE
integrations, custom agents, …) invoke `tasks` operations as tools.

- **Library:** [`spring-ai-starter-mcp-server-webmvc`](https://docs.spring.io/spring-ai/reference/api/mcp/mcp-server-boot-starter-docs.html)
  (Spring AI `2.0.0-M8`, aligned with Spring Boot 4).
- **Transport:** SSE over HTTP, hosted in the same Spring Boot process.
- **Adapter location:** `io.jaranas.kafkapoc.tasks.api.mcp` (API layer, peer to `controller/`).
- **Server type:** `SYNC`. Configured under `spring.ai.mcp.server.*` in `application.yml`.

### Endpoints

| Endpoint   | Purpose                                                |
|------------|--------------------------------------------------------|
| `/sse`     | SSE stream the MCP client subscribes to.               |
| `/mcp/**`  | JSON-RPC message endpoint used by the MCP client.      |

Both are currently **permitted without authentication** for this PoC (see
`SecurityConfig`). Securing the MCP transport is a follow-up.

### Tools exposed

All five tasks use cases are available with full parity with the REST controller. Because
the MCP transport has no Spring Security `Principal`, every tool requires `userId`
(UUIDv4) as an explicit argument.

| Tool name                | Use case            | Required arguments                                  |
|--------------------------|---------------------|-----------------------------------------------------|
| `tasks_create_task`      | Create task         | `taskId`, `userId`, `title`, `description`          |
| `tasks_get_task`         | Get task            | `taskId`, `userId`                                  |
| `tasks_list_user_tasks`  | List user tasks     | `userId`                                            |
| `tasks_complete_task`    | Complete task       | `taskId`, `userId`                                  |
| `tasks_archive_task`     | Archive task        | `taskId`, `userId`                                  |

`tasks_create_task` is idempotent on `taskId`: if a task with the same id already exists
for that user, the existing one is returned instead of creating a new one.

### Connecting an MCP client

With the app running (Docker Compose or `./gradlew bootRun`), point any MCP client at the
SSE endpoint:

```
http://localhost:8080/sse
```

#### Claude Desktop (via SSE proxy)

Claude Desktop currently speaks the STDIO transport, so a small proxy
(e.g. [`mcp-proxy`](https://github.com/sparfenyuk/mcp-proxy)) is needed to bridge to SSE.
Example `claude_desktop_config.json` snippet:

```json
{
  "mcpServers": {
    "kafka-poc-tasks": {
      "command": "mcp-proxy",
      "args": ["http://localhost:8080/sse"]
    }
  }
}
```

#### Generic SSE-capable MCP client

Just configure the server URL:

```
sse: http://localhost:8080/sse
```

### Quick smoke test

Verify that the SSE endpoint is reachable (the connection should stay open and stream an
initial `endpoint` event):

```bash
curl -N http://localhost:8080/sse
```

### Invoking a tool (example)

Once the client is connected, listing the user's tasks looks like this from the MCP
client side:

```json
{
  "name": "tasks_list_user_tasks",
  "arguments": {
    "userId": "00000000-0000-0000-0000-000000000001"
  }
}
```

Creating a task:

```json
{
  "name": "tasks_create_task",
  "arguments": {
    "taskId": "11111111-1111-4111-8111-111111111111",
    "userId": "00000000-0000-0000-0000-000000000001",
    "title": "My first task",
    "description": "Created via MCP"
  }
}
```

---

## Kafka Topics

Topic naming convention: `<domain>.<event>.v<version>` (kebab-case). Record keys are UUIDv4.

| Topic                       | Produced by | Consumed by     |
|-----------------------------|-------------|-----------------|
| `tasks.task-created.v1`     | `tasks`     | `notifications` |
| `tasks.task-completed.v1`   | `tasks`     | `notifications` |
| `tasks.task-archived.v1`    | `tasks`     | `notifications` |

---

## Learning Roadmap

1. **Bootstrap** — Gradle + Spring Boot 4 skeleton, Dockerfile, Docker Compose (this milestone). ✅
2. **Tasks domain — REST + Mongo** — CRUD without Kafka yet. ✅
3. **Tasks domain — Kafka producer** — publish `task-created`, `task-completed`, `task-archived`.
4. **Notifications domain — Kafka consumer** — materialize notifications in Mongo.
5. **Notifications domain — REST reads** — GET / PATCH (read) / DELETE (archive).
6. **Resilience** — retries, dead-letter topic, idempotent consumers.
7. **Observability** — Actuator metrics, structured logs, topic dashboards.

---

## AI Agent Guidelines

This repo is designed to be worked on with AI coding agents (Junie, Copilot, Claude, Codex, …).
The single source of truth for agent rules is [`AGENTS.md`](AGENTS.md); platform-specific files
(`CLAUDE.md`, `codex.md`, `.github/copilot-instructions.md`, etc.) just delegate to it.

Testing conventions live in [`docs/TESTING.md`](docs/TESTING.md).

---

## License

See [`LICENSE`](LICENSE).
