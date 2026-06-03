# AGENTS.md — AI Agent Guidelines

This document defines the rules, conventions and architecture guidelines that any AI coding agent
(Junie, GitHub Copilot, Claude Code, Codex, etc.) **must** follow when working on this project.

> **Platform-specific files** (`.junie/guidelines.md`, `.github/copilot-instructions.md`,
> `CLAUDE.md`, `codex.md`) all point back to this file as the single source of truth.

---

## Table of Contents

1. [Project Overview](#project-overview)
2. [Domains](#domains)
3. [Architecture](#architecture)
4. [Coding Rules](#coding-rules)
5. [Git & Commits](#git--commits)
6. [Documentation](#documentation)
7. [Reference Documents](#reference-documents)

---

## Project Overview

- **Language:** Kotlin
- **Framework:** Spring Boot 4 (Spring Data MongoDB, Spring for Apache Kafka)
- **Build tool:** Gradle (Kotlin DSL)
- **Database:** MongoDB
- **Messaging:** Apache Kafka (Spring for Apache Kafka)
- **Containerization:** Docker + Docker Compose
- **Java version:** 17
- **Base package:** `io.jaranas.kafkapoc`
- **Gradle project name:** `io.jaranas.kafka-poc`
- **API documentation:** SpringDoc OpenAPI (Swagger UI) — to be added when the first REST
  endpoint exists.
- **Security:** Spring Security — to be added when the first secured endpoint exists.

---

## Domains

All domains follow the **hexagonal architecture** pattern and live under
`io.jaranas.kafkapoc.<domain>` so that Spring Boot's default component scan (rooted at
`io.jaranas.kafkapoc`) detects all beans automatically.

Current domains:

- `io.jaranas.kafkapoc.tasks` — task management (REST CRUD + Kafka producer).
- `io.jaranas.kafkapoc.notifications` — Kafka consumer that materializes notifications from
  `tasks` events.

New domains **must** replicate the same package structure and visibility rules described in
[docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

---

## Architecture

Full architecture documentation is available at **[docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)**.

### Hexagonal Architecture — Summary

```
api.controller / api.model                          ← API layer (REST)
        │
application.usecase / application.model             ← Application layer (use cases)
        │
domain.model / domain.service / domain.repository / domain.event   ← Domain layer (core)
        │
infrastructure.repository.impl / infrastructure.messaging.{producer,consumer,model}
        ← Infrastructure layer (MongoDB + Kafka adapters)
```

### Layer Visibility Rules

| Layer | Can see |
|---|---|
| **Domain** | Nothing else — no `infrastructure`, `application` or `api` |
| **Application** | `domain` only — no `infrastructure` or `api` |
| **API** | `application` (and exceptionally `domain.service` / `domain.repository` for simple CRUDs) |
| **Infrastructure** | `domain` only |

---

## Coding Rules

1. **Language:** Kotlin — always use **named parameters** when possible.
2. **Trailing commas:** Always add a trailing comma after the last element in multi-line parameter lists,
   argument lists, destructuring declarations, and `when` entries. This applies to function definitions,
   function calls, constructors, data classes, and any other position where Kotlin allows it.
3. **Documentation & code:** Always in **English**.
4. **Max line length:** **120 characters**.
5. **IDs:** Use **UUIDv4** across all layers.
6. **HTTP methods:**
   - `PUT` for creation (idempotent).
   - `PATCH` for partial updates (e.g. completing a task).
   - `DELETE` for archiving / soft-deleting.
   - `GET` for reads.
7. **RESTful design:** Follow REST conventions for URL naming and HTTP semantics.
8. **Authentication:** Use Spring Security's `Principal` (from the authenticated user) — never
   custom headers like `X-User-Id`.
9. **Domain repositories** are **interfaces only** — implementations live in
   `infrastructure.repository.impl`.
10. **Code style:** Follow the existing style in the module/file you are editing.
11. **Kafka topics:** Name topics as `<domain>.<event>.v<version>` in **kebab-case**
    (e.g. `tasks.task-created.v1`). Bump the `vN` suffix on breaking schema changes.
12. **Kafka keys:** Use **UUIDv4** as record keys (consistent with the IDs rule).
13. **Kafka adapters:** Producers and consumers live in `infrastructure.messaging.producer`
    and `infrastructure.messaging.consumer`. They depend only on `domain`.
14. **Event ports:** Publisher interfaces (ports) live in `domain.event`; Kafka producers in
    `infrastructure.messaging.producer` implement them.

---

## Git & Commits

- Use **Conventional Commits** format: `type(scope): description`.
  - `scope` should be the **domain** or area affected (e.g. `tasks`, `accounts`, `auth`).
  - Types:
    - `feat`: A new feature.
    - `fix`: A bug fix.
    - `docs`: Documentation only changes.
    - `style`: Changes that do not affect the meaning of the code (white-space, formatting, etc.).
    - `refactor`: A code change that neither fixes a bug nor adds a feature.
    - `perf`: A code change that improves performance.
    - `test`: Adding missing tests or correcting existing tests.
    - `build`: Changes that affect the build system or external dependencies (e.g. Gradle, Docker).
    - `ci`: Changes to CI configuration files and scripts.
    - `chore`: Other changes that don't modify src or test files.
    - `revert`: Reverts a previous commit.
  - Examples:
    ```
    feat(tasks): add archive task use case
    fix(accounts): correct balance calculation on transfer
    docs(agents.md): add AGENTS.md guidelines and architecture documentation
    refactor(tasks): extract validation logic to domain service
    ```
- Every commit made by an AI agent **must** include a co-author trailer:
  ```
  --trailer "Co-authored-by: <AgentName> <<agent-email>>"
  ```
  Examples:
  ```
  --trailer "Co-authored-by: Junie <junie@jetbrains.com>"
  --trailer "Co-authored-by: GitHub Copilot <copilot@github.com>"
  --trailer "Co-authored-by: Claude <claude@anthropic.com>"
  ```
- Use **git flow** branching model (`feature/`, `release/`, `hotfix/`).

---

## Documentation

- Keep `README.md` up to date with any new endpoints or configuration changes.
- Domain-specific documentation goes in each domain's own `AGENTS.md`.
- Global domain registry: [docs/DOMAINS.md](docs/DOMAINS.md).
- Architecture details: [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

---

## Reference Documents

| Document | Purpose |
|---|---|
| [`AGENTS.md`](AGENTS.md) | This file — global agent guidelines |
| [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) | Detailed architecture documentation |
| [`docs/DOMAINS.md`](docs/DOMAINS.md) | Domain registry and descriptions |
| `src/main/kotlin/io/jaranas/kafkapoc/tasks/AGENTS.md` | Tasks domain guidelines (to be created) |
| `src/main/kotlin/io/jaranas/kafkapoc/notifications/AGENTS.md` | Notifications domain guidelines (to be created) |
