package io.jaranas.kafkapoc.notifications.api.controller

import io.jaranas.kafkapoc.notifications.api.model.NotificationResponseDto
import io.jaranas.kafkapoc.notifications.api.model.toResponseDto
import io.jaranas.kafkapoc.notifications.application.usecase.ArchiveNotificationUseCase
import io.jaranas.kafkapoc.notifications.application.usecase.GetNotificationUseCase
import io.jaranas.kafkapoc.notifications.application.usecase.ListUserNotificationsUseCase
import io.jaranas.kafkapoc.notifications.application.usecase.MarkNotificationAsReadUseCase
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal
import java.util.UUID

@RestController
@RequestMapping("/api/notifications")
class NotificationController(
    private val listUserNotificationsUseCase: ListUserNotificationsUseCase,
    private val getNotificationUseCase: GetNotificationUseCase,
    private val markNotificationAsReadUseCase: MarkNotificationAsReadUseCase,
    private val archiveNotificationUseCase: ArchiveNotificationUseCase,
) {

    @GetMapping
    fun listNotifications(principal: Principal): ResponseEntity<List<NotificationResponseDto>> {
        val notifications = listUserNotificationsUseCase(userId = UUID.fromString(principal.name))
        return ResponseEntity.ok(notifications.map { it.toResponseDto() })
    }

    @GetMapping("/{notificationId}")
    fun getNotification(
        @PathVariable notificationId: UUID,
        principal: Principal,
    ): ResponseEntity<NotificationResponseDto> {
        val notification = getNotificationUseCase(
            notificationId = notificationId,
            userId = UUID.fromString(principal.name),
        )
        return ResponseEntity.ok(notification.toResponseDto())
    }

    @PatchMapping("/{notificationId}/read")
    fun markAsRead(
        @PathVariable notificationId: UUID,
        principal: Principal,
    ): ResponseEntity<NotificationResponseDto> {
        val notification = markNotificationAsReadUseCase(
            notificationId = notificationId,
            userId = UUID.fromString(principal.name),
        )
        return ResponseEntity.ok(notification.toResponseDto())
    }

    @DeleteMapping("/{notificationId}")
    fun archiveNotification(
        @PathVariable notificationId: UUID,
        principal: Principal,
    ): ResponseEntity<Void> {
        archiveNotificationUseCase(
            notificationId = notificationId,
            userId = UUID.fromString(principal.name),
        )
        return ResponseEntity.noContent().build()
    }
}
