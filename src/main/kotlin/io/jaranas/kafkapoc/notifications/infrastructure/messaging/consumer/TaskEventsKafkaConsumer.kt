package io.jaranas.kafkapoc.notifications.infrastructure.messaging.consumer

import io.jaranas.kafkapoc.notifications.application.usecase.MaterializeNotificationFromTaskArchivedUseCase
import io.jaranas.kafkapoc.notifications.application.usecase.MaterializeNotificationFromTaskCompletedUseCase
import io.jaranas.kafkapoc.notifications.application.usecase.MaterializeNotificationFromTaskCreatedUseCase
import io.jaranas.kafkapoc.notifications.infrastructure.messaging.model.TaskArchivedEventDto
import io.jaranas.kafkapoc.notifications.infrastructure.messaging.model.TaskCompletedEventDto
import io.jaranas.kafkapoc.notifications.infrastructure.messaging.model.TaskCreatedEventDto
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
class TaskEventsKafkaConsumer(
    private val materializeFromTaskCreated: MaterializeNotificationFromTaskCreatedUseCase,
    private val materializeFromTaskCompleted: MaterializeNotificationFromTaskCompletedUseCase,
    private val materializeFromTaskArchived: MaterializeNotificationFromTaskArchivedUseCase,
) {

    @KafkaListener(topics = ["tasks.task-created.v1"], groupId = "notifications")
    fun onTaskCreated(dto: TaskCreatedEventDto) {
        materializeFromTaskCreated.execute(
            taskId = dto.taskId,
            userId = dto.userId,
            createdAt = dto.createdAt,
        )
    }

    @KafkaListener(topics = ["tasks.task-completed.v1"], groupId = "notifications")
    fun onTaskCompleted(dto: TaskCompletedEventDto) {
        materializeFromTaskCompleted.execute(
            taskId = dto.taskId,
            userId = dto.userId,
            completedAt = dto.completedAt,
        )
    }

    @KafkaListener(topics = ["tasks.task-archived.v1"], groupId = "notifications")
    fun onTaskArchived(dto: TaskArchivedEventDto) {
        materializeFromTaskArchived.execute(
            taskId = dto.taskId,
            userId = dto.userId,
            archivedAt = dto.archivedAt,
        )
    }
}
