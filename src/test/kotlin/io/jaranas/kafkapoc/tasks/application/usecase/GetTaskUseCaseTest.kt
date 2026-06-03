package io.jaranas.kafkapoc.tasks.application.usecase

import io.jaranas.kafkapoc.tasks.domain.model.TaskMother
import io.jaranas.kafkapoc.tasks.domain.service.TaskService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GetTaskUseCaseTest {

    private val taskService: TaskService = mockk()
    private val useCase = GetTaskUseCase(taskService = taskService)

    @Test
    fun `should return task from service`() {
        // given
        val task = TaskMother.random(userId = "user-1")
        every { taskService.findByIdForUser(taskId = task.id, userId = "user-1") } returns task

        // when
        val result = useCase(taskId = task.id, userId = "user-1")

        // then
        assertEquals(task, result)
    }
}
