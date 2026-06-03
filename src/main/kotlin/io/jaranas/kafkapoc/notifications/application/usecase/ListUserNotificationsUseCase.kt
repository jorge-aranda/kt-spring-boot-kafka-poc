package io.jaranas.kafkapoc.notifications.application.usecase

import io.jaranas.kafkapoc.notifications.domain.model.Notification
import io.jaranas.kafkapoc.notifications.domain.service.NotificationService
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class ListUserNotificationsUseCase(
    private val notificationService: NotificationService,
) {

    operator fun invoke(userId: UUID): List<Notification> =
        notificationService.listActiveForUser(userId = userId)
}
