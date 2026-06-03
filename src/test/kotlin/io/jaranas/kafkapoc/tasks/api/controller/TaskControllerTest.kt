package io.jaranas.kafkapoc.tasks.api.controller

import io.jaranas.kafkapoc.tasks.application.usecase.ArchiveTaskUseCase
import io.jaranas.kafkapoc.tasks.application.usecase.CompleteTaskUseCase
import io.jaranas.kafkapoc.tasks.application.usecase.CreateTaskUseCase
import io.jaranas.kafkapoc.tasks.application.usecase.GetTaskUseCase
import io.jaranas.kafkapoc.tasks.application.usecase.ListUserTasksUseCase
import io.jaranas.kafkapoc.tasks.domain.exception.TaskNotFoundException
import io.jaranas.kafkapoc.tasks.domain.model.TaskMother
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import tools.jackson.databind.json.JsonMapper
import java.security.Principal
import java.util.UUID

class TaskControllerTest {

    private val createTaskUseCase: CreateTaskUseCase = mockk()
    private val getTaskUseCase: GetTaskUseCase = mockk()
    private val listUserTasksUseCase: ListUserTasksUseCase = mockk()
    private val completeTaskUseCase: CompleteTaskUseCase = mockk()
    private val archiveTaskUseCase: ArchiveTaskUseCase = mockk()

    private val controller = TaskController(
        createTaskUseCase = createTaskUseCase,
        getTaskUseCase = getTaskUseCase,
        listUserTasksUseCase = listUserTasksUseCase,
        completeTaskUseCase = completeTaskUseCase,
        archiveTaskUseCase = archiveTaskUseCase,
    )

    private lateinit var mockMvc: MockMvc
    private val objectMapper = JsonMapper.builder().build()
    private val userId = UUID.randomUUID().toString()
    private val principal: Principal = mockk()

    @BeforeEach
    fun setUp() {
        every { principal.name } returns userId
        mockMvc = MockMvcBuilders
            .standaloneSetup(controller)
            .setControllerAdvice(TaskExceptionHandler())
            .setMessageConverters(JacksonJsonHttpMessageConverter(objectMapper))
            .build()
    }

    @Test
    fun `should return 201 when creating a new task`() {
        // given
        val taskId = UUID.randomUUID().toString()
        val task = TaskMother.random(id = taskId, userId = userId)
        every { getTaskUseCase(taskId = taskId, userId = userId) } throws TaskNotFoundException(taskId = taskId)
        every { createTaskUseCase(request = any()) } returns task
        val body = """{"title": "My task", "description": "desc"}"""

        // when / then
        mockMvc.perform(
            put("/api/tasks/$taskId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .principal(principal),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(taskId))
    }

    @Test
    fun `should return 200 with existing task when PUT-ing an existing taskId (idempotent)`() {
        // given
        val taskId = UUID.randomUUID().toString()
        val task = TaskMother.random(id = taskId, userId = userId)
        every { getTaskUseCase(taskId = taskId, userId = userId) } returns task
        val body = """{"title": "My task", "description": "desc"}"""

        // when / then
        mockMvc.perform(
            put("/api/tasks/$taskId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body)
                .principal(principal),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(taskId))
    }

    @Test
    fun `should return 200 with user tasks on GET api tasks`() {
        // given
        val tasks = listOf(TaskMother.random(userId = userId), TaskMother.random(userId = userId))
        every { listUserTasksUseCase(userId = userId) } returns tasks

        // when / then
        mockMvc.perform(
            get("/api/tasks").principal(principal),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.length()").value(2))
    }

    @Test
    fun `should return 404 when getting a non-existing task`() {
        // given
        val taskId = UUID.randomUUID().toString()
        every { getTaskUseCase(taskId = taskId, userId = userId) } throws TaskNotFoundException(taskId = taskId)

        // when / then
        mockMvc.perform(
            get("/api/tasks/$taskId").principal(principal),
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `should return 200 on PATCH complete`() {
        // given
        val task = TaskMother.random(userId = userId)
        val completed = task.copy(completed = true)
        every { completeTaskUseCase(taskId = task.id, userId = userId) } returns completed

        // when / then
        mockMvc.perform(
            patch("/api/tasks/${task.id}/complete").principal(principal),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.completed").value(true))
    }

    @Test
    fun `should return 204 on DELETE archive`() {
        // given
        val task = TaskMother.random(userId = userId)
        every { archiveTaskUseCase(taskId = task.id, userId = userId) } returns task.copy(archived = true)

        // when / then
        mockMvc.perform(
            delete("/api/tasks/${task.id}").principal(principal),
        )
            .andExpect(status().isNoContent)
    }
}
