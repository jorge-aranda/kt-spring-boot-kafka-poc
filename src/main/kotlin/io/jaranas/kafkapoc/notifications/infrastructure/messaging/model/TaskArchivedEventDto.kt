package io.jaranas.kafkapoc.notifications.infrastructure.messaging.model

import java.time.Instant
import java.util.UUID

data class TaskArchivedEventDto(
    val taskId: UUID,
    val userId: UUID,
    val archivedAt: Instant,
)
