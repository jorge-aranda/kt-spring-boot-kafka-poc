package io.jaranas.kafkapoc.tasks.application.usecase

import io.jaranas.kafkapoc.tasks.domain.model.Task
import io.jaranas.kafkapoc.tasks.domain.service.TaskService
import org.springframework.stereotype.Component

@Component
class CompleteTaskUseCase(
    private val taskService: TaskService,
) {

    fun execute(taskId: String, userId: String): Task =
        taskService.complete(taskId = taskId, userId = userId)
}
