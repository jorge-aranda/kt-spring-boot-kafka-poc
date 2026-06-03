package io.jaranas.kafkapoc.tasks.domain.model

import java.time.Instant
import java.util.UUID

data class Task(
    val id: UUID,
    val userId: UUID,
    val title: String,
    val description: String,
    val completed: Boolean,
    val archived: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
)
