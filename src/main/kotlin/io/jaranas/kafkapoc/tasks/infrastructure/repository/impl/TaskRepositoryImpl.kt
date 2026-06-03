package io.jaranas.kafkapoc.tasks.infrastructure.repository.impl

import io.jaranas.kafkapoc.tasks.domain.model.Task
import io.jaranas.kafkapoc.tasks.domain.repository.TaskRepository
import io.jaranas.kafkapoc.tasks.infrastructure.model.TaskDbo
import io.jaranas.kafkapoc.tasks.infrastructure.repository.TaskMongoDboRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class TaskRepositoryImpl(
    private val taskMongoDboRepository: TaskMongoDboRepository,
) : TaskRepository {

    override fun save(task: Task): Task =
        taskMongoDboRepository.save(task.toDbo()).toDomain()

    override fun findById(id: UUID): Task? =
        taskMongoDboRepository.findById(id).orElse(null)?.toDomain()

    override fun findByUserIdAndArchivedFalse(userId: UUID): List<Task> =
        taskMongoDboRepository.findByUserIdAndArchivedFalse(userId = userId).map { it.toDomain() }
}

private fun Task.toDbo(): TaskDbo = TaskDbo(
    id = id,
    userId = userId,
    title = title,
    description = description,
    completed = completed,
    archived = archived,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

private fun TaskDbo.toDomain(): Task = Task(
    id = id,
    userId = userId,
    title = title,
    description = description,
    completed = completed,
    archived = archived,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
