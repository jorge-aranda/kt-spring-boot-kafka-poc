package io.jaranas.kafkapoc.tasks.domain.repository

import io.jaranas.kafkapoc.tasks.domain.model.Task

interface TaskRepository {
    fun save(task: Task): Task
    fun findById(id: String): Task?
    fun findByUserIdAndArchivedFalse(userId: String): List<Task>
}
