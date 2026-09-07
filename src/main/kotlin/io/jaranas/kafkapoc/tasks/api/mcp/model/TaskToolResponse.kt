package io.jaranas.kafkapoc.tasks.api.mcp.model

import io.jaranas.kafkapoc.tasks.domain.model.Task
import java.time.Instant
import java.util.UUID

data class TaskToolResponse(
    val id: UUID,
    val userId: UUID,
    val title: String,
    val description: String,
    val completed: Boolean,
    val archived: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
)

fun Task.toToolResponse(): TaskToolResponse = TaskToolResponse(
    id = id,
    userId = userId,
    title = title,
    description = description,
    completed = completed,
    archived = archived,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
