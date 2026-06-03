# Domains

This document lists all bounded contexts (domains) in the project.

---

## Domain Registry

| Domain | Package | Architecture | Description |
|---|---|---|---|
| **Tasks** | `io.jaranas.kafkapoc.tasks` | Hexagonal | Task management per user, publishes Kafka events |
| **Notifications** | `io.jaranas.kafkapoc.notifications` | Hexagonal | Consumes `tasks` events, exposes read API |

---

## Tasks

- **Status:** In progress (milestone 1: REST CRUD + Mongo, Kafka pending).
- **Package:** `io.jaranas.kafkapoc.tasks`
- **Architecture:** Hexagonal (reference implementation for new domains).
- **Domain-specific guidelines:**
  [`src/main/kotlin/io/jaranas/kafkapoc/tasks/AGENTS.md`](../src/main/kotlin/io/jaranas/kafkapoc/tasks/AGENTS.md)
- **Note:** Kafka events publishing not implemented yet — coming in next milestone.

### Capabilities

| Use Case | HTTP | Endpoint |
|---|---|---|
| Create a task | `PUT` | `/api/tasks/{taskId}` |
| List user tasks | `GET` | `/api/tasks` |
| Get task detail | `GET` | `/api/tasks/{taskId}` |
| Complete a task | `PATCH` | `/api/tasks/{taskId}/complete` |
| Archive a task | `DELETE` | `/api/tasks/{taskId}` |
| Create a task group | `PUT` | `/api/task-groups` |
| List user task groups | `GET` | `/api/task-groups` |
| Get task group detail | `GET` | `/api/task-groups/{taskGroupId}` |
| Add task to group | `PATCH` | `/api/task-groups/{taskGroupId}/tasks/{taskId}` |
| Remove task from group | `DELETE` | `/api/task-groups/{taskGroupId}/tasks/{taskId}` |
| Archive a task group | `DELETE` | `/api/task-groups/{taskGroupId}` |

### Kafka Events Published

| Topic | Key | Payload (summary) | Triggering use case |
|---|---|---|---|
| `tasks.task-created.v1` | `taskId` (UUIDv4) | `{ taskId, userId, title, createdAt }` | `CreateTaskUseCase` |
| `tasks.task-completed.v1` | `taskId` (UUIDv4) | `{ taskId, userId, completedAt }` | `CompleteTaskUseCase` |
| `tasks.task-archived.v1` | `taskId` (UUIDv4) | `{ taskId, userId, archivedAt }` | `ArchiveTaskUseCase` |

### Key Decisions

- Uses `UUIDv4` for all identifiers and Kafka record keys.
- User identification via Spring Security `Principal`.
- Soft-delete (archive) instead of hard delete.
- Idempotent creation via `PUT`.
- Adding a task already in a group is a no-op (idempotent).
- Event publishing port (`TaskEventPublisher`) lives in `tasks.domain.event`; the Kafka
  implementation lives in `tasks.infrastructure.messaging.producer`.

---

## Notifications

- **Status:** Planned (to be implemented).
- **Package:** `io.jaranas.kafkapoc.notifications`
- **Architecture:** Hexagonal — Kafka-driven (no write endpoints).
- **Domain-specific guidelines:**
  `src/main/kotlin/io/jaranas/kafkapoc/notifications/AGENTS.md` (to be created with the domain).

### Capabilities

| Use Case | HTTP | Endpoint |
|---|---|---|
| List user notifications | `GET` | `/api/notifications` |
| Get notification detail | `GET` | `/api/notifications/{notificationId}` |
| Mark as read | `PATCH` | `/api/notifications/{notificationId}/read` |
| Archive notification | `DELETE` | `/api/notifications/{notificationId}` |

### Kafka Topics Consumed

| Topic | Consumer group | Reaction |
|---|---|---|
| `tasks.task-created.v1` | `notifications` | Materialize a "task created" notification |
| `tasks.task-completed.v1` | `notifications` | Materialize a "task completed" notification |
| `tasks.task-archived.v1` | `notifications` | Materialize a "task archived" notification |

### Key Decisions

- The domain has **no write REST endpoints**; notifications are materialized exclusively from
  Kafka events.
- Single consumer group (`notifications`) for all `tasks.*.v1` topics.
- Idempotent consumption keyed by `taskId` + event type to tolerate retries / duplicates.
- Consumers live in `notifications.infrastructure.messaging.consumer` and invoke application
  use cases (e.g. `MaterializeNotificationFromTaskCreatedUseCase`).

---

## Adding a New Domain

1. Create the package `io.jaranas.kafkapoc.<domain>` following the hexagonal structure defined
   in [docs/ARCHITECTURE.md](ARCHITECTURE.md).
2. Use the `tasks` domain as the reference implementation.
3. Add a domain-specific `AGENTS.md` inside the domain's source directory.
4. Register the new domain in this file (including any Kafka topics it publishes or consumes).
5. Update `README.md` with the new endpoints and topics.
