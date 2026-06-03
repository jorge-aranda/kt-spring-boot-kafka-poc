package io.jaranas.kafkapoc.tasks.domain.service

import io.jaranas.kafkapoc.tasks.domain.event.TaskArchivedEvent
import io.jaranas.kafkapoc.tasks.domain.event.TaskCompletedEvent
import io.jaranas.kafkapoc.tasks.domain.event.TaskCreatedEvent
import io.jaranas.kafkapoc.tasks.domain.event.TaskEventPublisher
import io.jaranas.kafkapoc.tasks.domain.exception.TaskNotFoundException
import io.jaranas.kafkapoc.tasks.domain.model.Task
import io.jaranas.kafkapoc.tasks.domain.repository.TaskRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class TaskService(
    private val taskRepository: TaskRepository,
    private val taskEventPublisher: TaskEventPublisher,
) {

    fun create(task: Task): Task {
        val existing = taskRepository.findById(id = task.id)
        if (existing != null) {
            if (existing.userId != task.userId) throw TaskNotFoundException(taskId = task.id)
            return existing
        }
        val saved = taskRepository.save(task = task)
        taskEventPublisher.publish(
            event = TaskCreatedEvent(
                taskId = saved.id,
                userId = saved.userId,
                title = saved.title,
                createdAt = saved.createdAt,
            ),
        )
        return saved
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
        val now = Instant.now()
        val saved = taskRepository.save(task = task.copy(completed = true, updatedAt = now))
        taskEventPublisher.publish(
            event = TaskCompletedEvent(
                taskId = saved.id,
                userId = saved.userId,
                completedAt = now,
            ),
        )
        return saved
    }

    fun archive(taskId: UUID, userId: UUID): Task {
        val task = findByIdForUser(taskId = taskId, userId = userId)
        if (task.archived) return task
        val now = Instant.now()
        val saved = taskRepository.save(task = task.copy(archived = true, updatedAt = now))
        taskEventPublisher.publish(
            event = TaskArchivedEvent(
                taskId = saved.id,
                userId = saved.userId,
                archivedAt = now,
            ),
        )
        return saved
    }
}
