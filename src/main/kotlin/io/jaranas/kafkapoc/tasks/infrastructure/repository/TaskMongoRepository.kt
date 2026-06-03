package io.jaranas.kafkapoc.tasks.infrastructure.repository

import org.springframework.data.mongodb.repository.MongoRepository

interface TaskMongoRepository : MongoRepository<TaskDocument, String> {
    fun findByUserIdAndArchivedFalse(userId: String): List<TaskDocument>
}
