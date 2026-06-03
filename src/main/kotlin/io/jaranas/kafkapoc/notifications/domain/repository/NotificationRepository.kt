package io.jaranas.kafkapoc.notifications.domain.repository

import io.jaranas.kafkapoc.notifications.domain.model.Notification
import io.jaranas.kafkapoc.notifications.domain.model.NotificationType
import java.util.UUID

interface NotificationRepository {
    fun save(notification: Notification): Notification
    fun findById(id: UUID): Notification?
    fun findByTaskIdAndType(taskId: UUID, type: NotificationType): Notification?
    fun findByUserIdAndArchivedFalse(userId: UUID): List<Notification>
}
