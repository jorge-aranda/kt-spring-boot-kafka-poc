package io.jaranas.kafkapoc.tasks.infrastructure.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document(collection = "tasks")
@CompoundIndex(name = "user_archived_idx", def = "{ 'userId': 1, 'archived': 1 }")
data class TaskDbo(
    @Id val id: String,
    val userId: String,
    val title: String,
    val description: String,
    val completed: Boolean,
    val archived: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
)
