package io.jaranas.kafkapoc.tasks.api.model

import io.jaranas.kafkapoc.tasks.domain.model.Task
import java.time.Instant

data class TaskResponseDto(
    val id: String,
    val userId: String,
    val title: String,
    val description: String,
    val completed: Boolean,
    val archived: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
)

fun Task.toResponseDto(): TaskResponseDto = TaskResponseDto(
    id = id,
    userId = userId,
    title = title,
    description = description,
    completed = completed,
    archived = archived,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
