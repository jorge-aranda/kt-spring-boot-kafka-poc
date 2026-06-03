package io.jaranas.kafkapoc.tasks.domain.repository

import io.jaranas.kafkapoc.tasks.domain.model.Task
import java.util.UUID

class FakeTaskRepository : TaskRepository {
    private val store = mutableMapOf<UUID, Task>()

    override fun save(task: Task): Task {
        store[task.id] = task
        return task
    }

    override fun findById(id: UUID): Task? = store[id]

    override fun findByUserIdAndArchivedFalse(userId: UUID): List<Task> =
        store.values.filter { it.userId == userId && !it.archived }
}
