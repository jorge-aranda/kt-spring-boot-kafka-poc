package io.jaranas.kafkapoc.tasks.domain.exception

class TaskNotFoundException(taskId: String) : RuntimeException("Task not found: $taskId")
