package io.jaranas.kafkapoc.tasks.application.usecase

import io.jaranas.kafkapoc.tasks.application.model.TaskRequest
import io.jaranas.kafkapoc.tasks.domain.model.Task
import io.jaranas.kafkapoc.tasks.domain.service.TaskService
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class CreateTaskUseCase(
    private val taskService: TaskService,
) {

    fun execute(request: TaskRequest): Task {
        val now = Instant.now()
        val task = Task(
            id = request.taskId,
            userId = request.userId,
            title = request.title,
            description = request.description,
            completed = false,
            archived = false,
            createdAt = now,
            updatedAt = now,
        )
        return taskService.create(task = task)
    }
}
