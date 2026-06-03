package io.jaranas.kafkapoc.tasks.infrastructure.messaging.producer

import io.jaranas.kafkapoc.tasks.domain.event.TaskArchivedEvent
import io.jaranas.kafkapoc.tasks.domain.event.TaskCompletedEvent
import io.jaranas.kafkapoc.tasks.domain.event.TaskCreatedEvent
import io.jaranas.kafkapoc.tasks.domain.event.TaskEventPublisher
import io.jaranas.kafkapoc.tasks.infrastructure.messaging.model.TaskArchivedEventDto
import io.jaranas.kafkapoc.tasks.infrastructure.messaging.model.TaskCompletedEventDto
import io.jaranas.kafkapoc.tasks.infrastructure.messaging.model.TaskCreatedEventDto
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class TaskEventKafkaPublisher(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
) : TaskEventPublisher {

    override fun publish(event: TaskCreatedEvent) {
        val dto = TaskCreatedEventDto(
            taskId = event.taskId,
            userId = event.userId,
            title = event.title,
            createdAt = event.createdAt,
        )
        kafkaTemplate.send(TOPIC_TASK_CREATED, event.taskId.toString(), dto)
    }

    override fun publish(event: TaskCompletedEvent) {
        val dto = TaskCompletedEventDto(
            taskId = event.taskId,
            userId = event.userId,
            completedAt = event.completedAt,
        )
        kafkaTemplate.send(TOPIC_TASK_COMPLETED, event.taskId.toString(), dto)
    }

    override fun publish(event: TaskArchivedEvent) {
        val dto = TaskArchivedEventDto(
            taskId = event.taskId,
            userId = event.userId,
            archivedAt = event.archivedAt,
        )
        kafkaTemplate.send(TOPIC_TASK_ARCHIVED, event.taskId.toString(), dto)
    }

    companion object {
        const val TOPIC_TASK_CREATED = "tasks.task-created.v1"
        const val TOPIC_TASK_COMPLETED = "tasks.task-completed.v1"
        const val TOPIC_TASK_ARCHIVED = "tasks.task-archived.v1"
    }
}
