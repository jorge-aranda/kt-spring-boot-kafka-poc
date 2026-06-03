package io.jaranas.kafkapoc.tasks.application.usecase

import io.jaranas.kafkapoc.tasks.domain.model.TaskMother
import io.jaranas.kafkapoc.tasks.domain.service.TaskService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.UUID

class CompleteTaskUseCaseTest {

    private val taskService: TaskService = mockk()
    private val useCase = CompleteTaskUseCase(taskService = taskService)

    @Test
    fun `should invoke taskService complete and return result`() {
        // given
        val userId = UUID.randomUUID()
        val task = TaskMother.random(userId = userId)
        val completed = task.copy(completed = true)
        every { taskService.complete(taskId = task.id, userId = userId) } returns completed

        // when
        val result = useCase(taskId = task.id, userId = userId)

        // then
        assertEquals(completed, result)
    }
}
