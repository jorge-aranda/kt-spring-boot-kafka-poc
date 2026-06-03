package io.jaranas.kafkapoc.tasks.domain.service

import io.jaranas.kafkapoc.tasks.domain.exception.TaskNotFoundException
import io.jaranas.kafkapoc.tasks.domain.model.Task
import io.jaranas.kafkapoc.tasks.domain.repository.TaskRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class TaskService(
    private val taskRepository: TaskRepository,
) {

    fun create(task: Task): Task {
        val existing = taskRepository.findById(id = task.id)
        if (existing != null) {
            if (existing.userId != task.userId) throw TaskNotFoundException(taskId = task.id)
            return existing
        }
        return taskRepository.save(task = task)
    }

    fun findByIdForUser(taskId: UUID, userId: UUID): Task {
        val task = taskRepository.findById(id = taskId) ?: throw TaskNotFoundException(taskId = taskId)
        if (task.userId != userId) throw TaskNotFoundException(taskId = taskId)
        return task
    }

    fun listActiveForUser(userId: UUID): List<Task> =
        taskRepository.findByUserIdAndArchivedFalse(userId = userId)

    fun complete(taskId: UUID, userId: UUID): Task {
        val task = findByIdForUser(taskId = taskId, userId = userId)
        if (task.completed) return task
        return taskRepository.save(task = task.copy(completed = true, updatedAt = Instant.now()))
    }

    fun archive(taskId: UUID, userId: UUID): Task {
        val task = findByIdForUser(taskId = taskId, userId = userId)
        if (task.archived) return task
        return taskRepository.save(task = task.copy(archived = true, updatedAt = Instant.now()))
    }
}
