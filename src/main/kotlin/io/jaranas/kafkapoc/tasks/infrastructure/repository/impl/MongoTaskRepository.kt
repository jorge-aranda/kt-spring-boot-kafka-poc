package io.jaranas.kafkapoc.tasks.infrastructure.repository.impl

import io.jaranas.kafkapoc.tasks.domain.model.Task
import io.jaranas.kafkapoc.tasks.domain.repository.TaskRepository
import io.jaranas.kafkapoc.tasks.infrastructure.repository.TaskDocument
import io.jaranas.kafkapoc.tasks.infrastructure.repository.TaskMongoRepository
import org.springframework.stereotype.Component

@Component
class MongoTaskRepository(
    private val taskMongoRepository: TaskMongoRepository,
) : TaskRepository {

    override fun save(task: Task): Task =
        taskMongoRepository.save(task.toDocument()).toDomain()

    override fun findById(id: String): Task? =
        taskMongoRepository.findById(id).orElse(null)?.toDomain()

    override fun findByUserIdAndArchivedFalse(userId: String): List<Task> =
        taskMongoRepository.findByUserIdAndArchivedFalse(userId = userId).map { it.toDomain() }
}

private fun Task.toDocument(): TaskDocument = TaskDocument(
    id = id,
    userId = userId,
    title = title,
    description = description,
    completed = completed,
    archived = archived,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

private fun TaskDocument.toDomain(): Task = Task(
    id = id,
    userId = userId,
    title = title,
    description = description,
    completed = completed,
    archived = archived,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
