package io.jaranas.kafkapoc.tasks.infrastructure.repository

import io.jaranas.kafkapoc.tasks.infrastructure.model.TaskDbo
import org.springframework.data.mongodb.repository.MongoRepository

interface TaskMongoRepository : MongoRepository<TaskDbo, String> {
    fun findByUserIdAndArchivedFalse(userId: String): List<TaskDbo>
}
