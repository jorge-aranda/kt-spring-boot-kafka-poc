package io.jaranas.kafkapoc.tasks.application.model

import java.util.UUID

object TaskRequestMother {
    fun random(
        taskId: String = UUID.randomUUID().toString(),
        userId: String = UUID.randomUUID().toString(),
        title: String = "Task title",
        description: String = "Task description",
    ): TaskRequest = TaskRequest(
        taskId = taskId,
        userId = userId,
        title = title,
        description = description,
    )
}
