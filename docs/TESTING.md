# Testing Guidelines

This document defines the testing conventions and rules for the project.

---

## Table of Contents

1. [General Rules](#general-rules)
2. [Test Scopes](#test-scopes)
3. [Test Structure — Given / When / Then](#test-structure--given--when--then)
4. [ObjectMother Pattern](#objectmother-pattern)
5. [Mocks vs Fakes](#mocks-vs-fakes)
6. [Reference Test](#reference-test)

---

## General Rules

- **Framework:** JUnit 5 + [MockK](https://mockk.io/) for mocking.
- **Language:** Kotlin — use **named parameters** in all calls.
- **Pattern:** Always use the **given / when / then** pattern (see below). **Never** use `whenever` (Mockito style).
- **Naming:** Test method names use backtick-quoted descriptive sentences, e.g. `` `should create a task from request` ``.

---

## Test Scopes

The build splits tests into three Gradle source sets to keep fast unit tests isolated
from the slower, infrastructure-bound ones:

| Scope | Source set | Gradle task | Requires Docker | Purpose |
|---|---|---|---|---|
| Unit | `src/test/kotlin` | `./gradlew test` | No | Pure unit tests for domain services, use cases, controllers (MockMvc) and exception handlers. Use **MockK** and in-memory **fakes**. |
| Integration | `src/integrationTest/kotlin` | `./gradlew integrationTest` | Yes | Boot the Spring context against a real **MongoDB** and **Kafka** spun up by [Testcontainers](https://testcontainers.com/). Exercise individual infrastructure adapters (`TaskRepositoryImpl`, `TaskEventKafkaPublisher`, …). |
| End-to-end | `src/e2eTest/kotlin` | `./gradlew e2eTest` | Yes | Boot the **full application** on a random port and exercise the public HTTP API end-to-end, including the cross-domain Kafka pipeline `tasks → notifications`. |

`./gradlew check` runs all three scopes; `./gradlew test` is intentionally limited to
unit tests so the local feedback loop stays fast.

### Conventions per scope

- **Unit tests** never touch infrastructure. Use `FakeTaskRepository`-style fakes for the
  domain layer and MockK for use-case dependencies.
- **Integration tests** extend `io.jaranas.kafkapoc.support.IntegrationTestBase`, which
  starts the Mongo and Kafka containers once per JVM and wires their addresses via
  `@DynamicPropertySource`. Each test cleans the Mongo collections it touches in
  `@BeforeEach`.
- **End-to-end tests** extend `io.jaranas.kafkapoc.support.E2ETestBase`. Spring Boot 4
  no longer ships `TestRestTemplate`, so e2e tests drive the API through a tiny
  `E2EHttpClient` built on top of `RestClient` and inject the random port with
  `@LocalServerPort`. HTTP Basic credentials use the configured dev user.
- **Asynchronous assertions** in e2e tests (Kafka → Mongo materialisation) use
  [Awaitility](https://github.com/awaitility/awaitility) — never `Thread.sleep`.

### File naming

- Unit test classes: `<Subject>Test.kt` (e.g. `CreateTaskUseCaseTest`).
- Integration test classes: `<Subject>IntegrationTest.kt` (e.g. `TaskRepositoryImplIntegrationTest`).
- End-to-end test classes: `<Feature>E2ETest.kt` (e.g. `TasksApiE2ETest`).

---

## Test Structure — Given / When / Then

Every test must be structured in three clearly separated blocks using comments:

```kotlin
@Test
fun `should do something`() {
    // given
    val input = SomeMother.random()

    // when
    val result = useCase(input = input)

    // then
    assertEquals(expected, result)
}
```

- **given** — set up preconditions (create test data, configure mocks).
- **when** — execute the action under test.
- **then** — assert the expected outcome.

When the `when` and `then` blocks are trivially combined (e.g. asserting an exception), use `// when / then` as a single comment.

---

## ObjectMother Pattern

ObjectMothers provide factory methods to create test instances of domain and application models.

### Rules

1. ObjectMother classes are **`object` singletons** named `<Model>Mother` (e.g. `TaskMother`, `TaskRequestMother`).
2. They live in the **test source set**, in the **same package** as the original model they create.
   - Domain model `io.jaranas.kafkapoc.tasks.domain.model.Task` → ObjectMother at
     `src/test/kotlin/io/jaranas/kafkapoc/tasks/domain/model/TaskMother.kt`.
   - Application model `io.jaranas.kafkapoc.tasks.application.model.TaskRequest` → ObjectMother
     at `src/test/kotlin/io/jaranas/kafkapoc/tasks/application/model/TaskRequestMother.kt`.
3. The primary factory method is `random(...)` with **all parameters having default values**, so callers only override what matters for their test.
4. Additional convenience methods (e.g. `completed()`, `archived()`) can be added for common scenarios.

### Example

```kotlin
object TaskMother {
    fun random(
        id: UUID = UUID.randomUUID(),
        userId: UUID = UUID.randomUUID(),
        title: String = "Task title",
        description: String = "Task description",
        completed: Boolean = false,
        archived: Boolean = false,
        createdAt: Instant = Instant.parse("2025-01-01T00:00:00Z"),
        updatedAt: Instant = Instant.parse("2025-01-01T00:00:00Z"),
    ): Task = Task(
        id = id,
        userId = userId,
        title = title,
        description = description,
        completed = completed,
        archived = archived,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
```

---

## Mocks vs Fakes

| Layer | Strategy | Rationale |
|---|---|---|
| **Application (use cases)** | **MockK mocks** | Use cases orchestrate domain services; mocking allows precise control of interactions. |
| **Domain (services)** | **Fakes** preferred, mocks acceptable | Domain services are often thin; a `FakeTaskRepository` (in-memory `Map`) is simpler and more readable than mocking every repository call. |
| **Infrastructure** | **Never fake** | Infrastructure implementations are not faked or mocked in unit tests. Integration tests should cover them separately. |

### Fake example

```kotlin
class FakeTaskRepository : TaskRepository {
    private val store = mutableMapOf<UUID, Task>()

    override fun save(task: Task): Task { store[task.id] = task; return task }
    override fun findById(id: UUID): Task? = store[id]
    override fun findByUserIdAndArchivedFalse(userId: UUID): List<Task> =
        store.values.filter { it.userId == userId && !it.archived }
}
```

### Mock example (MockK)

```kotlin
private val taskService: TaskService = mockk()

every { taskService.findById(id = task.id) } returns task
every { taskService.complete(task = task) } returns task.copy(completed = true)
```

> **Important:** Always use `every { ... }` from MockK. **Never** use `whenever` from Mockito.

---

## Reference Test

Use **`CreateTaskUseCaseTest`** as the canonical reference for writing new use case tests:

**File:** `src/test/kotlin/io/jaranas/kafkapoc/tasks/application/usecase/CreateTaskUseCaseTest.kt`

```kotlin
class CreateTaskUseCaseTest {

    private val taskService: TaskService = mockk()
    private val useCase = CreateTaskUseCase(taskService = taskService)

    @Test
    fun `should create a task from request`() {
        // given
        val userId = UUID.randomUUID()
        val request = TaskRequestMother.random(
            userId = userId,
            title = "My task",
            description = "desc",
        )
        val taskSlot = slot<Task>()
        every { taskService.create(task = capture(taskSlot)) } answers { taskSlot.captured }

        // when
        val result = useCase(request = request)

        // then
        assertEquals(userId, result.userId)
        assertEquals("My task", result.title)
        assertEquals("desc", result.description)
        assertNotNull(result.id)
    }
}
```

This test demonstrates:
- MockK mock for `TaskService` dependency.
- ObjectMother (`TaskRequestMother`) for test data creation.
- `given / when / then` structure with clear comments.
- Named parameters in all calls.
- `slot` + `capture` + `answers` pattern when you need to return the same object that was passed in.

When creating tests for a new use case, replicate this structure and adapt it to the specific use case logic.
