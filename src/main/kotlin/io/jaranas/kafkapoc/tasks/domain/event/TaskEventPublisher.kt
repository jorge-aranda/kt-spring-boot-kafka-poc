package io.jaranas.kafkapoc.tasks.domain.event

interface TaskEventPublisher {
    fun publish(event: TaskCreatedEvent)
    fun publish(event: TaskCompletedEvent)
    fun publish(event: TaskArchivedEvent)
}
