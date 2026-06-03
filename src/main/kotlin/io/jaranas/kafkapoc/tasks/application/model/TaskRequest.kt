package io.jaranas.kafkapoc.tasks.application.model

data class TaskRequest(
    val taskId: String,
    val userId: String,
    val title: String,
    val description: String,
)
