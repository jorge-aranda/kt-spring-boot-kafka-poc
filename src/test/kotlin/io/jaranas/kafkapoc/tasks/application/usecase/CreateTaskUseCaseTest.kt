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
import java.util.UUID

class CreateTaskUseCaseTest {

    private val taskService: TaskService = mockk()
    private val useCase = CreateTaskUseCase(taskService = taskService)

    @Test
    fun `should create a task from request`() {
        // given
        val userId = UUID.randomUUID()
        val request = TaskRequestMother.random(
            userId = userId,
            title = "My task",
            description = "desc",
        )
        val taskSlot = slot<Task>()
        every { taskService.create(task = capture(taskSlot)) } answers { taskSlot.captured }

        // when
        val result = useCase(request = request)

        // then
        assertEquals(userId, result.userId)
        assertEquals("My task", result.title)
        assertEquals("desc", result.description)
        assertNotNull(result.id)
    }
}
