package io.jaranas.kafkapoc.tasks.domain.service

import io.jaranas.kafkapoc.tasks.domain.event.FakeTaskEventPublisher
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
import java.util.UUID

class TaskServiceTest {

    private lateinit var taskRepository: FakeTaskRepository
    private lateinit var taskEventPublisher: FakeTaskEventPublisher
    private lateinit var taskService: TaskService

    @BeforeEach
    fun setUp() {
        taskRepository = FakeTaskRepository()
        taskEventPublisher = FakeTaskEventPublisher()
        taskService = TaskService(
            taskRepository = taskRepository,
            taskEventPublisher = taskEventPublisher,
        )
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
        assertEquals(1, taskEventPublisher.createdEvents.size)
        assertEquals(task.id, taskEventPublisher.createdEvents.first().taskId)
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
        assertTrue(taskEventPublisher.createdEvents.isEmpty())
    }

    @Test
    fun `should throw TaskNotFoundException when creating with an id owned by another user`() {
        // given
        val existing = TaskMother.random()
        taskRepository.save(task = existing)
        val conflicting = existing.copy(userId = UUID.randomUUID())

        // when / then
        assertThrows<TaskNotFoundException> {
            taskService.create(task = conflicting)
        }
    }

    @Test
    fun `should list only non-archived tasks for the given user`() {
        // given
        val userId = UUID.randomUUID()
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
        val userId = UUID.randomUUID()
        val task = TaskMother.random(userId = userId)
        taskRepository.save(task = task)

        // when
        val result = taskService.complete(taskId = task.id, userId = userId)

        // then
        assertTrue(result.completed)
        assertTrue(result.updatedAt.isAfter(task.updatedAt) || result.updatedAt == task.updatedAt)
        assertEquals(1, taskEventPublisher.completedEvents.size)
        assertEquals(task.id, taskEventPublisher.completedEvents.first().taskId)
    }

    @Test
    fun `should be no-op when completing an already completed task`() {
        // given
        val userId = UUID.randomUUID()
        val task = TaskMother.completed(userId = userId)
        taskRepository.save(task = task)

        // when
        val result = taskService.complete(taskId = task.id, userId = userId)

        // then
        assertTrue(result.completed)
        assertEquals(task, result)
        assertTrue(taskEventPublisher.completedEvents.isEmpty())
    }

    @Test
    fun `should archive a task and update updatedAt`() {
        // given
        val userId = UUID.randomUUID()
        val task = TaskMother.random(userId = userId)
        taskRepository.save(task = task)

        // when
        val result = taskService.archive(taskId = task.id, userId = userId)

        // then
        assertTrue(result.archived)
        assertFalse(result.completed)
        assertEquals(1, taskEventPublisher.archivedEvents.size)
        assertEquals(task.id, taskEventPublisher.archivedEvents.first().taskId)
    }

    @Test
    fun `should throw TaskNotFoundException when archiving a task of another user`() {
        // given
        val userId = UUID.randomUUID()
        val task = TaskMother.random(userId = userId)
        taskRepository.save(task = task)

        // when / then
        assertThrows<TaskNotFoundException> {
            taskService.archive(taskId = task.id, userId = UUID.randomUUID())
        }
    }
}
