package io.jaranas.kafkapoc.notifications.api.model

import io.jaranas.kafkapoc.notifications.domain.model.Notification
import io.jaranas.kafkapoc.notifications.domain.model.NotificationType
import java.time.Instant
import java.util.UUID

data class NotificationResponseDto(
    val id: UUID,
    val userId: UUID,
    val taskId: UUID,
    val type: NotificationType,
    val read: Boolean,
    val archived: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
)

fun Notification.toResponseDto(): NotificationResponseDto = NotificationResponseDto(
    id = id,
    userId = userId,
    taskId = taskId,
    type = type,
    read = read,
    archived = archived,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
