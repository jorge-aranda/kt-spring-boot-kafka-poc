package io.jaranas.kafkapoc.tasks.application.model

import java.util.UUID

object TaskRequestMother {
    fun random(
        taskId: UUID = UUID.randomUUID(),
        userId: UUID = UUID.randomUUID(),
        title: String = "Task title",
        description: String = "Task description",
    ): TaskRequest = TaskRequest(
        taskId = taskId,
        userId = userId,
        title = title,
        description = description,
    )
}
