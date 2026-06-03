package io.jaranas.kafkapoc.notifications.infrastructure.repository.impl

import io.jaranas.kafkapoc.notifications.domain.model.Notification
import io.jaranas.kafkapoc.notifications.domain.model.NotificationType
import io.jaranas.kafkapoc.notifications.domain.repository.NotificationRepository
import io.jaranas.kafkapoc.notifications.infrastructure.repository.NotificationDbo
import io.jaranas.kafkapoc.notifications.infrastructure.repository.NotificationMongoDboRepository
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class NotificationRepositoryImpl(
    private val notificationMongoDboRepository: NotificationMongoDboRepository,
) : NotificationRepository {

    override fun save(notification: Notification): Notification =
        notificationMongoDboRepository.save(notification.toDbo()).toDomain()

    override fun findById(id: UUID): Notification? =
        notificationMongoDboRepository.findById(id).orElse(null)?.toDomain()

    override fun findByTaskIdAndType(taskId: UUID, type: NotificationType): Notification? =
        notificationMongoDboRepository.findByTaskIdAndType(taskId = taskId, type = type)?.toDomain()

    override fun findByUserIdAndArchivedFalse(userId: UUID): List<Notification> =
        notificationMongoDboRepository.findByUserIdAndArchivedFalse(userId = userId).map { it.toDomain() }
}

private fun Notification.toDbo(): NotificationDbo = NotificationDbo(
    id = id,
    userId = userId,
    taskId = taskId,
    type = type,
    read = read,
    archived = archived,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

private fun NotificationDbo.toDomain(): Notification = Notification(
    id = id,
    userId = userId,
    taskId = taskId,
    type = type,
    read = read,
    archived = archived,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
