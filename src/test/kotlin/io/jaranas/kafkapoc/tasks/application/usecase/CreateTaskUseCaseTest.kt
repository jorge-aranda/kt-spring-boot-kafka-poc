package io.jaranas.kafkapoc.tasks.application.usecase

import io.jaranas.kafkapoc.tasks.application.model.TaskRequestMother
import io.jaranas.kafkapoc.tasks.domain.model.Task
import io.jaranas.kafkapoc.tasks.domain.service.TaskService
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class CreateTaskUseCaseTest {

    private val taskService: TaskService = mockk()
    private val useCase = CreateTaskUseCase(taskService = taskService)

    @Test
    fun `should create a task from request`() {
        // given
        val request = TaskRequestMother.random(
            userId = "user-1",
            title = "My task",
            description = "desc",
        )
        val taskSlot = slot<Task>()
        every { taskService.create(task = capture(taskSlot)) } answers { taskSlot.captured }

        // when
        val result = useCase(request = request)

        // then
        assertEquals("user-1", result.userId)
        assertEquals("My task", result.title)
        assertEquals("desc", result.description)
        assertNotNull(result.id)
    }
}
