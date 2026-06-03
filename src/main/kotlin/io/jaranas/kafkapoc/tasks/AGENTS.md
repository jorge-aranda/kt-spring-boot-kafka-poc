# Tasks Domain — Agent Guidelines

This document defines the rules and conventions specific to the `tasks` domain.
It complements the global [`AGENTS.md`](../../../../../AGENTS.md) and
[`docs/ARCHITECTURE.md`](../../../../../docs/ARCHITECTURE.md).

---

## Package Structure

```
io.jaranas.kafkapoc.tasks
├── api/
│   ├── controller/          ← TaskController, TaskExceptionHandler
│   └── model/               ← CreateTaskRequestDto, TaskResponseDto
├── application/
│   ├── model/               ← TaskRequest
│   └── usecase/             ← CreateTaskUseCase, GetTaskUseCase, ListUserTasksUseCase,
│                               CompleteTaskUseCase, ArchiveTaskUseCase
├── domain/
│   ├── exception/           ← TaskNotFoundException
│   ├── model/               ← Task
│   ├── repository/          ← TaskRepository (port interface)
│   └── service/             ← TaskService
└── infrastructure/
    ├── model/               ← TaskDbo
    └── repository/
        ├── TaskMongoRepository.kt
        └── impl/MongoTaskRepository.kt
```

---

## Key Design Decisions

### Idempotent Creation (`PUT /api/tasks/{taskId}`)

- The client generates the `taskId` (UUIDv4) and sends it in the URL.
- If the task already exists and belongs to the authenticated user, the existing task is returned (200).
- If the task exists but belongs to another user, a `TaskNotFoundException` is thrown (404).
- If the task does not exist, it is created and returned (201).

### User Ownership via `Principal`

- All endpoints require authentication (HTTP Basic in this milestone).
- `userId` is always obtained from `principal.name` — never from request headers or body.
- `TaskService` enforces ownership: any operation on a task belonging to a different user throws
  `TaskNotFoundException` (404, to avoid revealing existence).

### Soft Delete (Archive)

- `DELETE /api/tasks/{taskId}` sets `archived = true` and updates `updatedAt`.
- `GET /api/tasks` only returns tasks with `archived = false`.
- `GET /api/tasks/{taskId}` returns the task regardless of `archived` status.
- Archiving an already-archived task is a no-op (idempotent).

### No Kafka in Milestone 1

- `domain.event` and `infrastructure.messaging.*` are **not** present in this milestone.
- `TaskService` has no event-publishing dependencies.
- Kafka integration will be added in the next milestone by introducing a `TaskEventPublisher`
  port in `domain.event` without changing the public API of `TaskService`.

---

## Testing

Follow [`docs/TESTING.md`](../../../../../docs/TESTING.md) for all testing conventions.

| Layer | Strategy |
|---|---|
| `domain.service` | Fakes (`FakeTaskRepository` in-memory) |
| `application.usecase` | MockK mocks of `TaskService` |
| `api.controller` | MockK mocks of use cases + `MockMvcBuilders.standaloneSetup` |
| `infrastructure` | No unit tests in this milestone (no integration tests either) |

ObjectMothers:
- `TaskMother` → `src/test/kotlin/io/jaranas/kafkapoc/tasks/domain/model/TaskMother.kt`
- `TaskRequestMother` → `src/test/kotlin/io/jaranas/kafkapoc/tasks/application/model/TaskRequestMother.kt`
