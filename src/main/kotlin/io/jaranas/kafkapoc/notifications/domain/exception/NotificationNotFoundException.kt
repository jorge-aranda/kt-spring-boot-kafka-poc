package io.jaranas.kafkapoc.notifications.domain.exception

import java.util.UUID

class NotificationNotFoundException(notificationId: UUID) :
    RuntimeException("Notification not found: $notificationId")
