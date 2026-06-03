# Testing Guidelines

This document defines the testing conventions and rules for the project.

---

## Table of Contents

1. [General Rules](#general-rules)
2. [Test Structure — Given / When / Then](#test-structure--given--when--then)
3. [ObjectMother Pattern](#objectmother-pattern)
4. [Mocks vs Fakes](#mocks-vs-fakes)
5. [Reference Test](#reference-test)

---

## General Rules

- **Framework:** JUnit 5 + [MockK](https://mockk.io/) for mocking.
- **Language:** Kotlin — use **named parameters** in all calls.
- **Pattern:** Always use the **given / when / then** pattern (see below). **Never** use `whenever` (Mockito style).
- **Naming:** Test method names use backtick-quoted descriptive sentences, e.g. `` `should create a task from request` ``.

---

## Test Structure — Given / When / Then

Every test must be structured in three clearly separated blocks using comments:

```kotlin
@Test
fun `should do something`() {
    // given
    val input = SomeMother.random()

    // when
    val result = useCase.execute(input = input)

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
        id: String = UUID.randomUUID().toString(),
        userId: String = UUID.randomUUID().toString(),
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
    private val store = mutableMapOf<String, Task>()

    override fun save(task: Task): Task { store[task.id] = task; return task }
    override fun findById(id: String): Task? = store[id]
    override fun findByUserIdAndArchivedFalse(userId: String): List<Task> =
        store.values.filter { it.userId == userId && !it.archived }
    override fun deleteById(id: String) { store.remove(id) }
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
        val request = TaskRequestMother.random(
            userId = "user-1",
            title = "My task",
            description = "desc",
        )
        val taskSlot = slot<Task>()
        every { taskService.create(task = capture(taskSlot)) } answers { taskSlot.captured }

        // when
        val result = useCase.execute(request = request)

        // then
        assertEquals("user-1", result.userId)
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
