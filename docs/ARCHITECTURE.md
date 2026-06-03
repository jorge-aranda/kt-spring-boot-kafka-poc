# Architecture of the Kafka POC

This document describes the architecture used in the Kafka POC project.

---

## Overview

All domains follow a single architectural style: **hexagonal architecture**, with four clearly
separated layers (api, application, domain, infrastructure). The Spring Boot entry point is
`io.jaranas.kafkapoc.KafkaPocApplication`, and component scanning is rooted at
`io.jaranas.kafkapoc`.

Persistence uses **MongoDB**. Inter-domain communication uses **Apache Kafka** (via Spring for
Apache Kafka). Both are adapters living in the `infrastructure` layer.

---

## Hexagonal Architecture

All domains **must** follow the package structure below.

### Package layout (`io.jaranas.kafkapoc.<domain>`)

```
io.jaranas.kafkapoc.<domain>
├── api
│   ├── controller      # REST controllers
│   └── model           # DTOs (request/response)
├── application
│   ├── usecase         # Application use cases
│   └── model           # Application-level models
├── domain
│   ├── model           # Domain entities
│   ├── service         # Domain services
│   ├── repository      # Domain repository interfaces (ports)
│   └── event           # Event ports (publisher / handler interfaces)
└── infrastructure
    ├── repository
    │   ├── <Dbo>       # Spring Data MongoDB repositories + documents
    │   └── impl        # Domain repository implementations (adapters)
    └── messaging
        ├── producer    # KafkaTemplate-based publishers (implement domain.event ports)
        ├── consumer    # @KafkaListener consumers (invoke application use cases)
        └── model       # Serialization-only DTOs (event payloads on the wire)
```

### Layer Responsibilities

#### Domain Layer (`domain`)

The **core** of the bounded context. Contains:

- **Models** (`domain.model`): Pure domain entities with business attributes. Use `UUIDv4` for
  identifiers. No framework annotations.
- **Repository interfaces** (`domain.repository`): Ports that define persistence operations.
  Interfaces only — no implementation here.
- **Services** (`domain.service`): Domain business logic. Annotated with `@Service`. Depend
  only on domain repository interfaces and event ports.
- **Event ports** (`domain.event`): Publisher interfaces (e.g. `TaskEventPublisher`) and
  handler interfaces. Pure Kotlin — no Kafka types leak in.

> ⚠️ The domain layer **must not** import anything from `application`, `api` or
> `infrastructure`.

#### Application Layer (`application`)

Orchestrates use cases by coordinating domain services. Contains:

- **Use cases** (`application.usecase`): One class per use case, annotated with `@Component`.
  Each use case has a single public `execute(...)` method.
- **Models** (`application.model`): Application-level request/command objects, independent of
  the API layer DTOs.

> ⚠️ The application layer **must not** import anything from `api` or `infrastructure`.

#### API Layer (`api`)

The inbound HTTP adapter. Contains:

- **Controllers** (`api.controller`): `@RestController` classes that receive HTTP requests,
  convert DTOs to application models, invoke use cases, and return response DTOs.
- **DTOs** (`api.model`): Request and response data transfer objects. Decoupled from domain
  models.

> The API layer depends on `application`. Exceptionally, for simple CRUDs, it may access
> `domain.service` or `domain.repository` directly.

#### Infrastructure Layer (`infrastructure`)

The outbound (and inbound, for Kafka consumers) adapters. Contains:

- **Spring Data repositories** (`infrastructure.repository`): Interfaces extending
  `MongoRepository` with their corresponding document classes (e.g. `TaskDocument`).
- **Domain repository implementations** (`infrastructure.repository.impl`): `@Component`
  classes that implement the domain repository interface by delegating to the Spring Data
  repository and mapping between domain models and documents.
- **Kafka producers** (`infrastructure.messaging.producer`): `@Component` classes that
  implement the publisher interfaces from `domain.event`. They use `KafkaTemplate` to send
  records and translate domain events into wire DTOs from `infrastructure.messaging.model`.
- **Kafka consumers** (`infrastructure.messaging.consumer`): `@Component` classes with
  `@KafkaListener` methods. They are inbound adapters: they deserialize wire DTOs and invoke
  the corresponding application use case.
- **Messaging DTOs** (`infrastructure.messaging.model`): Serialization-only payloads for
  Kafka. They are not domain types.

> ⚠️ The infrastructure layer **must not** import anything from `application` or `api`.
> It depends only on `domain`. Kafka consumers are the single exception: they may import
> from `application.usecase` to invoke use cases as inbound adapters.

### Visibility Matrix

| Layer | Domain | Application | API | Infrastructure |
|---|---|---|---|---|
| **Domain** (incl. `domain.event`) | ✅ self | ❌ | ❌ | ❌ |
| **Application** | ✅ | ✅ self | ❌ | ❌ |
| **API** | ⚠️ limited | ✅ | ✅ self | ❌ |
| **Infrastructure (repository)** | ✅ | ❌ | ❌ | ✅ self |
| **Infrastructure (messaging.consumer)** | ✅ | ✅ (use cases only) | ❌ | ✅ self |

---

## Kafka & Messaging

Kafka is the inter-domain communication backbone of this POC. The hexagonal split is:

- **Publishing side (`tasks`):**
  - The `domain.event` package declares port interfaces such as `TaskEventPublisher` and event
    data classes (e.g. `TaskCreatedEvent`). They contain no Kafka types.
  - The implementation lives in `infrastructure.messaging.producer`. It uses
    `KafkaTemplate<String, ...>`, translates domain events into wire DTOs from
    `infrastructure.messaging.model`, and publishes to topics following the naming convention
    `<domain>.<event>.v<version>` (kebab-case, e.g. `tasks.task-created.v1`).
  - Record keys are **UUIDv4** (the aggregate id, e.g. `taskId`) to guarantee partition
    affinity and ordering per aggregate.

- **Consuming side (`notifications`):**
  - `infrastructure.messaging.consumer` exposes `@KafkaListener` methods. Each listener
    deserializes the wire DTO into a domain command and invokes the matching
    `application.usecase` (e.g. `MaterializeNotificationFromTaskCreatedUseCase`).
  - Consumers are inbound adapters; they own offset management, error handling and DLQ
    routing. The domain remains unaware of Kafka.

- **Schema evolution:** Breaking changes bump the `vN` suffix in the topic name. Wire DTOs in
  `infrastructure.messaging.model` are versioned independently from domain events.

---

## Creating a New Domain

1. Create the package `io.jaranas.kafkapoc.<domain>` with the full hexagonal structure shown
   above (including `domain.event` and `infrastructure.messaging.*` if the domain produces or
   consumes events).
2. Add a domain-specific `AGENTS.md` inside the domain's source directory.
3. Register the domain in [docs/DOMAINS.md](DOMAINS.md), including any Kafka topics it
   publishes or consumes.
4. Update `README.md` with the new endpoints and topics.
