package io.jaranas.kafkapoc.tasks.domain.event

class FakeTaskEventPublisher : TaskEventPublisher {

    val createdEvents = mutableListOf<TaskCreatedEvent>()
    val completedEvents = mutableListOf<TaskCompletedEvent>()
    val archivedEvents = mutableListOf<TaskArchivedEvent>()

    override fun publish(event: TaskCreatedEvent) {
        createdEvents.add(event)
    }

    override fun publish(event: TaskCompletedEvent) {
        completedEvents.add(event)
    }

    override fun publish(event: TaskArchivedEvent) {
        archivedEvents.add(event)
    }
}
