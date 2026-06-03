package io.jaranas.kafkapoc.tasks.domain.exception

import java.util.UUID

class TaskNotFoundException(taskId: UUID) : RuntimeException("Task not found: $taskId")
