package io.jaranas.kafkapoc.tasks.domain.repository

import io.jaranas.kafkapoc.tasks.domain.model.Task
import java.util.UUID

interface TaskRepository {
    fun save(task: Task): Task
    fun findById(id: UUID): Task?
    fun findByUserIdAndArchivedFalse(userId: UUID): List<Task>
}
