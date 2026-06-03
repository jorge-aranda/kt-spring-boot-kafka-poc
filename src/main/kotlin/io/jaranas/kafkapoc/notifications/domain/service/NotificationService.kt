package io.jaranas.kafkapoc.notifications.domain.service

import io.jaranas.kafkapoc.notifications.domain.exception.NotificationNotFoundException
import io.jaranas.kafkapoc.notifications.domain.model.Notification
import io.jaranas.kafkapoc.notifications.domain.model.NotificationType
import io.jaranas.kafkapoc.notifications.domain.repository.NotificationRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class NotificationService(
    private val notificationRepository: NotificationRepository,
) {

    fun materialize(notification: Notification): Notification {
        val existing = notificationRepository.findByTaskIdAndType(
            taskId = notification.taskId,
            type = notification.type,
        )
        if (existing != null) return existing
        return notificationRepository.save(notification = notification)
    }

    fun findByIdForUser(notificationId: UUID, userId: UUID): Notification {
        val notification = notificationRepository.findById(id = notificationId)
            ?: throw NotificationNotFoundException(notificationId = notificationId)
        if (notification.userId != userId) throw NotificationNotFoundException(notificationId = notificationId)
        return notification
    }

    fun listActiveForUser(userId: UUID): List<Notification> =
        notificationRepository.findByUserIdAndArchivedFalse(userId = userId)

    fun markAsRead(notificationId: UUID, userId: UUID): Notification {
        val notification = findByIdForUser(notificationId = notificationId, userId = userId)
        if (notification.read) return notification
        return notificationRepository.save(
            notification = notification.copy(read = true, updatedAt = Instant.now()),
        )
    }

    fun archive(notificationId: UUID, userId: UUID): Notification {
        val notification = findByIdForUser(notificationId = notificationId, userId = userId)
        if (notification.archived) return notification
        return notificationRepository.save(
            notification = notification.copy(archived = true, updatedAt = Instant.now()),
        )
    }
}
