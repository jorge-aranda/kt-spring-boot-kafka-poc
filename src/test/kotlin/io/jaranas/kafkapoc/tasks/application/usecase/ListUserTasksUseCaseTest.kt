package io.jaranas.kafkapoc.tasks.application.usecase

import io.jaranas.kafkapoc.tasks.domain.model.TaskMother
import io.jaranas.kafkapoc.tasks.domain.service.TaskService
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ListUserTasksUseCaseTest {

    private val taskService: TaskService = mockk()
    private val useCase = ListUserTasksUseCase(taskService = taskService)

    @Test
    fun `should return list of active tasks for user`() {
        // given
        val userId = "user-1"
        val tasks = listOf(TaskMother.random(userId = userId), TaskMother.random(userId = userId))
        every { taskService.listActiveForUser(userId = userId) } returns tasks

        // when
        val result = useCase(userId = userId)

        // then
        assertEquals(tasks, result)
    }
}
