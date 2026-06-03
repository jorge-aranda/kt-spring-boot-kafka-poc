package io.jaranas.kafkapoc.notifications.domain.service

import io.jaranas.kafkapoc.notifications.domain.exception.NotificationNotFoundException
import io.jaranas.kafkapoc.notifications.domain.model.NotificationMother
import io.jaranas.kafkapoc.notifications.domain.repository.FakeNotificationRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class NotificationServiceTest {

    private lateinit var notificationRepository: FakeNotificationRepository
    private lateinit var notificationService: NotificationService

    @BeforeEach
    fun setUp() {
        notificationRepository = FakeNotificationRepository()
        notificationService = NotificationService(notificationRepository = notificationRepository)
    }

    @Test
    fun `should materialize a new notification`() {
        // given
        val notification = NotificationMother.random()

        // when
        val result = notificationService.materialize(notification = notification)

        // then
        assertEquals(notification.id, result.id)
        assertEquals(notification.taskId, result.taskId)
        assertEquals(notification.type, result.type)
    }

    @Test
    fun `should be idempotent when materializing a notification with same taskId and type`() {
        // given
        val notification = NotificationMother.random()
        notificationRepository.save(notification = notification)

        // when
        val result = notificationService.materialize(notification = notification.copy(id = UUID.randomUUID()))

        // then
        assertEquals(notification.id, result.id)
    }

    @Test
    fun `should list only non-archived notifications for the given user`() {
        // given
        val userId = UUID.randomUUID()
        val active = NotificationMother.random(userId = userId)
        val archived = NotificationMother.archived(userId = userId)
        val otherUser = NotificationMother.random()
        notificationRepository.save(notification = active)
        notificationRepository.save(notification = archived)
        notificationRepository.save(notification = otherUser)

        // when
        val result = notificationService.listActiveForUser(userId = userId)

        // then
        assertEquals(1, result.size)
        assertEquals(active.id, result.first().id)
    }

    @Test
    fun `should mark a notification as read`() {
        // given
        val userId = UUID.randomUUID()
        val notification = NotificationMother.random(userId = userId)
        notificationRepository.save(notification = notification)

        // when
        val result = notificationService.markAsRead(notificationId = notification.id, userId = userId)

        // then
        assertTrue(result.read)
    }

    @Test
    fun `should be no-op when marking an already read notification`() {
        // given
        val userId = UUID.randomUUID()
        val notification = NotificationMother.read(userId = userId)
        notificationRepository.save(notification = notification)

        // when
        val result = notificationService.markAsRead(notificationId = notification.id, userId = userId)

        // then
        assertTrue(result.read)
        assertEquals(notification, result)
    }

    @Test
    fun `should archive a notification`() {
        // given
        val userId = UUID.randomUUID()
        val notification = NotificationMother.random(userId = userId)
        notificationRepository.save(notification = notification)

        // when
        val result = notificationService.archive(notificationId = notification.id, userId = userId)

        // then
        assertTrue(result.archived)
        assertFalse(result.read)
    }

    @Test
    fun `should throw NotificationNotFoundException when accessing notification of another user`() {
        // given
        val notification = NotificationMother.random()
        notificationRepository.save(notification = notification)

        // when / then
        assertThrows<NotificationNotFoundException> {
            notificationService.findByIdForUser(
                notificationId = notification.id,
                userId = UUID.randomUUID(),
            )
        }
    }

    @Test
    fun `should throw NotificationNotFoundException when notification does not exist`() {
        assertThrows<NotificationNotFoundException> {
            notificationService.findByIdForUser(
                notificationId = UUID.randomUUID(),
                userId = UUID.randomUUID(),
            )
        }
    }
}
