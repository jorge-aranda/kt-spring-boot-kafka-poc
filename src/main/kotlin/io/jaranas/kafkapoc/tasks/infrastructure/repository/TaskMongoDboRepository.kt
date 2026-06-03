package io.jaranas.kafkapoc.tasks.infrastructure.repository

import io.jaranas.kafkapoc.tasks.infrastructure.model.TaskDbo
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.UUID

interface TaskMongoDboRepository : MongoRepository<TaskDbo, UUID> {
    fun findByUserIdAndArchivedFalse(userId: UUID): List<TaskDbo>
}
