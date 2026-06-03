package io.jaranas.kafkapoc.tasks.application.usecase

import io.jaranas.kafkapoc.tasks.domain.model.TaskMother
import io.jaranas.kafkapoc.tasks.domain.service.TaskService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CompleteTaskUseCaseTest {

    private val taskService: TaskService = mockk()
    private val useCase = CompleteTaskUseCase(taskService = taskService)

    @Test
    fun `should invoke taskService complete and return result`() {
        // given
        val task = TaskMother.random(userId = "user-1")
        val completed = task.copy(completed = true)
        every { taskService.complete(taskId = task.id, userId = "user-1") } returns completed

        // when
        val result = useCase.execute(taskId = task.id, userId = "user-1")

        // then
        assertEquals(completed, result)
    }
}
