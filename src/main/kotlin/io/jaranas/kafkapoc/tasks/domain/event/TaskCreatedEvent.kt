package io.jaranas.kafkapoc.tasks.domain.event

import java.time.Instant
import java.util.UUID

data class TaskCreatedEvent(
    val taskId: UUID,
    val userId: UUID,
    val title: String,
    val createdAt: Instant,
)
