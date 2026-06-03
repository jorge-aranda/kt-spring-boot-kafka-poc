package io.jaranas.kafkapoc.notifications.domain.model

import java.time.Instant
import java.util.UUID

object NotificationMother {

    fun random(
        id: UUID = UUID.randomUUID(),
        userId: UUID = UUID.randomUUID(),
        taskId: UUID = UUID.randomUUID(),
        type: NotificationType = NotificationType.TASK_CREATED,
        read: Boolean = false,
        archived: Boolean = false,
        createdAt: Instant = Instant.now(),
        updatedAt: Instant = Instant.now(),
    ): Notification = Notification(
        id = id,
        userId = userId,
        taskId = taskId,
        type = type,
        read = read,
        archived = archived,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    fun read(userId: UUID = UUID.randomUUID()): Notification =
        random(userId = userId, read = true)

    fun archived(userId: UUID = UUID.randomUUID()): Notification =
        random(userId = userId, archived = true)
}
