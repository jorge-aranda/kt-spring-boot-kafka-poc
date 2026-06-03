package io.jaranas.kafkapoc.tasks.application.model

import java.util.UUID

data class TaskRequest(
    val taskId: UUID,
    val userId: UUID,
    val title: String,
    val description: String,
)
