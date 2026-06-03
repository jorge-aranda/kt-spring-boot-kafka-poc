package io.jaranas.kafkapoc.notifications.application.usecase

import io.jaranas.kafkapoc.notifications.domain.model.Notification
import io.jaranas.kafkapoc.notifications.domain.service.NotificationService
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class MarkNotificationAsReadUseCase(
    private val notificationService: NotificationService,
) {

    operator fun invoke(notificationId: UUID, userId: UUID): Notification =
        notificationService.markAsRead(notificationId = notificationId, userId = userId)
}
