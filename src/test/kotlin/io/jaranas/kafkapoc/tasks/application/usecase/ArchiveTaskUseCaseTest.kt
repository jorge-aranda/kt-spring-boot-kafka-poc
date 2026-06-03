package io.jaranas.kafkapoc.tasks.application.usecase

import io.jaranas.kafkapoc.tasks.domain.model.TaskMother
import io.jaranas.kafkapoc.tasks.domain.service.TaskService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.UUID

class ArchiveTaskUseCaseTest {

    private val taskService: TaskService = mockk()
    private val useCase = ArchiveTaskUseCase(taskService = taskService)

    @Test
    fun `should invoke taskService archive and return result`() {
        // given
        val userId = UUID.randomUUID()
        val task = TaskMother.random(userId = userId)
        val archived = task.copy(archived = true)
        every { taskService.archive(taskId = task.id, userId = userId) } returns archived

        // when
        val result = useCase(taskId = task.id, userId = userId)

        // then
        assertEquals(archived, result)
    }
}
