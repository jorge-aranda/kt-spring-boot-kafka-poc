package io.jaranas.kafkapoc.tasks.application.usecase

import io.jaranas.kafkapoc.tasks.domain.model.Task
import io.jaranas.kafkapoc.tasks.domain.service.TaskService
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class GetTaskUseCase(
    private val taskService: TaskService,
) {

    operator fun invoke(taskId: UUID, userId: UUID): Task =
        taskService.findByIdForUser(taskId = taskId, userId = userId)
}
