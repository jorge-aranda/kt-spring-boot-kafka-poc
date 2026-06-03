package io.jaranas.kafkapoc.tasks.domain.repository

import io.jaranas.kafkapoc.tasks.domain.model.Task

class FakeTaskRepository : TaskRepository {
    private val store = mutableMapOf<String, Task>()

    override fun save(task: Task): Task {
        store[task.id] = task
        return task
    }

    override fun findById(id: String): Task? = store[id]

    override fun findByUserIdAndArchivedFalse(userId: String): List<Task> =
        store.values.filter { it.userId == userId && !it.archived }
}
