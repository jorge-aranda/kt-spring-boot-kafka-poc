package io.jaranas.kafkapoc.notifications.application.usecase

import io.jaranas.kafkapoc.notifications.domain.model.Notification
import io.jaranas.kafkapoc.notifications.domain.model.NotificationType
import io.jaranas.kafkapoc.notifications.domain.service.NotificationService
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

@Component
class MaterializeNotificationFromTaskArchivedUseCase(
    private val notificationService: NotificationService,
) {

    fun execute(taskId: UUID, userId: UUID, archivedAt: Instant): Notification {
        val now = Instant.now()
        val notification = Notification(
            id = UUID.randomUUID(),
            userId = userId,
            taskId = taskId,
            type = NotificationType.TASK_ARCHIVED,
            read = false,
            archived = false,
            createdAt = now,
            updatedAt = now,
        )
        return notificationService.materialize(notification = notification)
    }
}
