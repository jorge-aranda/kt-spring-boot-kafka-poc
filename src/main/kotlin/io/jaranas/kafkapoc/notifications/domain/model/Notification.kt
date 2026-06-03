package io.jaranas.kafkapoc.notifications.domain.model

import java.time.Instant
import java.util.UUID

data class Notification(
    val id: UUID,
    val userId: UUID,
    val taskId: UUID,
    val type: NotificationType,
    val read: Boolean,
    val archived: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
)
