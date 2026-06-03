package io.jaranas.kafkapoc.tasks.domain.event

import java.time.Instant
import java.util.UUID

data class TaskArchivedEvent(
    val taskId: UUID,
    val userId: UUID,
    val archivedAt: Instant,
)
