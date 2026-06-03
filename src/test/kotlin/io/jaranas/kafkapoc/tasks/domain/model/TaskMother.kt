package io.jaranas.kafkapoc.tasks.domain.model

import java.time.Instant
import java.util.UUID

object TaskMother {
    fun random(
        id: UUID = UUID.randomUUID(),
        userId: UUID = UUID.randomUUID(),
        title: String = "Task title",
        description: String = "Task description",
        completed: Boolean = false,
        archived: Boolean = false,
        createdAt: Instant = Instant.parse("2025-01-01T00:00:00Z"),
        updatedAt: Instant = Instant.parse("2025-01-01T00:00:00Z"),
    ): Task = Task(
        id = id,
        userId = userId,
        title = title,
        description = description,
        completed = completed,
        archived = archived,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    fun completed(
        id: UUID = UUID.randomUUID(),
        userId: UUID = UUID.randomUUID(),
    ): Task = random(id = id, userId = userId, completed = true)

    fun archived(
        id: UUID = UUID.randomUUID(),
        userId: UUID = UUID.randomUUID(),
    ): Task = random(id = id, userId = userId, archived = true)
}
