package io.jaranas.kafkapoc.notifications.domain.repository

import io.jaranas.kafkapoc.notifications.domain.model.Notification
import io.jaranas.kafkapoc.notifications.domain.model.NotificationType
import java.util.UUID

class FakeNotificationRepository : NotificationRepository {

    private val store = mutableMapOf<UUID, Notification>()

    override fun save(notification: Notification): Notification {
        store[notification.id] = notification
        return notification
    }

    override fun findById(id: UUID): Notification? = store[id]

    override fun findByTaskIdAndType(taskId: UUID, type: NotificationType): Notification? =
        store.values.firstOrNull { it.taskId == taskId && it.type == type }

    override fun findByUserIdAndArchivedFalse(userId: UUID): List<Notification> =
        store.values.filter { it.userId == userId && !it.archived }
}
