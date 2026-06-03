package io.jaranas.kafkapoc.notifications.infrastructure.repository

import io.jaranas.kafkapoc.notifications.domain.model.NotificationType
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant
import java.util.UUID

@Document(collection = "notifications")
@CompoundIndexes(
    CompoundIndex(name = "user_archived_idx", def = "{ 'userId': 1, 'archived': 1 }"),
    CompoundIndex(name = "task_type_idx", def = "{ 'taskId': 1, 'type': 1 }", unique = true),
)
data class NotificationDbo(
    @Id val id: UUID,
    val userId: UUID,
    val taskId: UUID,
    val type: NotificationType,
    val read: Boolean,
    val archived: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
)
