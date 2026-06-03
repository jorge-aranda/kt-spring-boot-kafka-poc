package io.jaranas.kafkapoc.tasks.domain.service

import io.jaranas.kafkapoc.tasks.domain.exception.TaskNotFoundException
import io.jaranas.kafkapoc.tasks.domain.model.TaskMother
import io.jaranas.kafkapoc.tasks.domain.repository.FakeTaskRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TaskServiceTest {

    private lateinit var taskRepository: FakeTaskRepository
    private lateinit var taskService: TaskService

    @BeforeEach
    fun setUp() {
        taskRepository = FakeTaskRepository()
        taskService = TaskService(taskRepository = taskRepository)
    }

    @Test
    fun `should create a task with createdAt and updatedAt set`() {
        // given
        val task = TaskMother.random()

        // when
        val result = taskService.create(task = task)

        // then
        assertNotNull(result.id)
        assertEquals(task.userId, result.userId)
        assertEquals(task.title, result.title)
        assertNotNull(result.createdAt)
        assertNotNull(result.updatedAt)
    }

    @Test
    fun `should return existing task when creating with an id that already belongs to the user`() {
        // given
        val task = TaskMother.random()
        taskRepository.save(task = task)

        // when
        val result = taskService.create(task = task)

        // then
        assertEquals(task, result)
    }

    @Test
    fun `should throw TaskNotFoundException when creating with an id owned by another user`() {
        // given
        val existing = TaskMother.random()
        taskRepository.save(task = existing)
        val conflicting = existing.copy(userId = "other-user")

        // when / then
        assertThrows<TaskNotFoundException> {
            taskService.create(task = conflicting)
        }
    }

    @Test
    fun `should list only non-archived tasks for the given user`() {
        // given
        val userId = "user-1"
        val active = TaskMother.random(userId = userId)
        val archived = TaskMother.archived(userId = userId)
        val otherUser = TaskMother.random()
        taskRepository.save(task = active)
        taskRepository.save(task = archived)
        taskRepository.save(task = otherUser)

        // when
        val result = taskService.listActiveForUser(userId = userId)

        // then
        assertEquals(1, result.size)
        assertEquals(active.id, result.first().id)
    }

    @Test
    fun `should mark a task as completed and update updatedAt`() {
        // given
        val task = TaskMother.random(userId = "user-1")
        taskRepository.save(task = task)

        // when
        val result = taskService.complete(taskId = task.id, userId = "user-1")

        // then
        assertTrue(result.completed)
        assertTrue(result.updatedAt.isAfter(task.updatedAt) || result.updatedAt == task.updatedAt)
    }

    @Test
    fun `should be no-op when completing an already completed task`() {
        // given
        val task = TaskMother.completed(userId = "user-1")
        taskRepository.save(task = task)

        // when
        val result = taskService.complete(taskId = task.id, userId = "user-1")

        // then
        assertTrue(result.completed)
        assertEquals(task, result)
    }

    @Test
    fun `should archive a task and update updatedAt`() {
        // given
        val task = TaskMother.random(userId = "user-1")
        taskRepository.save(task = task)

        // when
        val result = taskService.archive(taskId = task.id, userId = "user-1")

        // then
        assertTrue(result.archived)
        assertFalse(result.completed)
    }

    @Test
    fun `should throw TaskNotFoundException when archiving a task of another user`() {
        // given
        val task = TaskMother.random(userId = "user-1")
        taskRepository.save(task = task)

        // when / then
        assertThrows<TaskNotFoundException> {
            taskService.archive(taskId = task.id, userId = "other-user")
        }
    }
}
