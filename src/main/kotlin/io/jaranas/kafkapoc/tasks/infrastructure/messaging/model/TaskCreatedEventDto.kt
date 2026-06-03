package io.jaranas.kafkapoc.tasks.infrastructure.messaging.model

import java.time.Instant
import java.util.UUID

data class TaskCreatedEventDto(
    val taskId: UUID,
    val userId: UUID,
    val title: String,
    val createdAt: Instant,
)
