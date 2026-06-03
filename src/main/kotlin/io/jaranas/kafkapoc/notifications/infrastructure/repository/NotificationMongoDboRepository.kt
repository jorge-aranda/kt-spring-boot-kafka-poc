package io.jaranas.kafkapoc.notifications.infrastructure.repository

import io.jaranas.kafkapoc.notifications.domain.model.NotificationType
import org.springframework.data.mongodb.repository.MongoRepository
import java.util.UUID

interface NotificationMongoDboRepository : MongoRepository<NotificationDbo, UUID> {
    fun findByTaskIdAndType(taskId: UUID, type: NotificationType): NotificationDbo?
    fun findByUserIdAndArchivedFalse(userId: UUID): List<NotificationDbo>
}
