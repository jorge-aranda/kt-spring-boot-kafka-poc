package io.jaranas.kafkapoc.tasks.domain.model

import java.time.Instant

data class Task(
    val id: String,
    val userId: String,
    val title: String,
    val description: String,
    val completed: Boolean,
    val archived: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
)
