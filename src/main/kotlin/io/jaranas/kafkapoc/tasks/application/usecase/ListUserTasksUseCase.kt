package io.jaranas.kafkapoc.tasks.application.usecase

import io.jaranas.kafkapoc.tasks.domain.model.Task
import io.jaranas.kafkapoc.tasks.domain.service.TaskService
import org.springframework.stereotype.Component

@Component
class ListUserTasksUseCase(
    private val taskService: TaskService,
) {

    operator fun invoke(userId: String): List<Task> =
        taskService.listActiveForUser(userId = userId)
}
