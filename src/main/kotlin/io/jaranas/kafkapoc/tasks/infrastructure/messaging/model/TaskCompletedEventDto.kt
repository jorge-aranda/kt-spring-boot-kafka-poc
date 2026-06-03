package io.jaranas.kafkapoc.tasks.infrastructure.messaging.model

import java.time.Instant
import java.util.UUID

data class TaskCompletedEventDto(
    val taskId: UUID,
    val userId: UUID,
    val completedAt: Instant,
)
