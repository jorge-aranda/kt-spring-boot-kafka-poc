package io.jaranas.kafkapoc.notifications.application.usecase

import io.jaranas.kafkapoc.notifications.domain.model.Notification
import io.jaranas.kafkapoc.notifications.domain.service.NotificationService
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class GetNotificationUseCase(
    private val notificationService: NotificationService,
) {

    operator fun invoke(notificationId: UUID, userId: UUID): Notification =
        notificationService.findByIdForUser(notificationId = notificationId, userId = userId)
}
