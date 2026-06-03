package io.jaranas.kafkapoc.notifications.application.usecase

import io.jaranas.kafkapoc.notifications.domain.model.Notification
import io.jaranas.kafkapoc.notifications.domain.model.NotificationType
import io.jaranas.kafkapoc.notifications.domain.service.NotificationService
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class MaterializeNotificationFromTaskCompletedUseCaseTest {

    private val notificationService: NotificationService = mockk()
    private val useCase = MaterializeNotificationFromTaskCompletedUseCase(notificationService = notificationService)

    @Test
    fun `should materialize a TASK_COMPLETED notification`() {
        // given
        val taskId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val slot = slot<Notification>()
        every { notificationService.materialize(notification = capture(slot)) } answers { slot.captured }

        // when
        val result = useCase.execute(taskId = taskId, userId = userId, completedAt = Instant.now())

        // then
        assertEquals(taskId, result.taskId)
        assertEquals(userId, result.userId)
        assertEquals(NotificationType.TASK_COMPLETED, result.type)
        assertFalse(result.read)
        assertFalse(result.archived)
    }
}
