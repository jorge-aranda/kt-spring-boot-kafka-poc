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

> All endpoints below are **planned** — they will land as each domain is implemented.

### Tasks (`io.jaranas.kafkapoc.tasks`)

REST CRUD over MongoDB, publishes events to Kafka.

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
2. **Tasks domain — REST + Mongo** — CRUD without Kafka yet.
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
