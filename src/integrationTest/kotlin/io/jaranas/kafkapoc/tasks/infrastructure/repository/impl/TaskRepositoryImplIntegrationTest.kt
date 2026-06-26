package io.jaranas.kafkapoc.tasks.infrastructure.repository.impl

import io.jaranas.kafkapoc.support.IntegrationTestBase
import io.jaranas.kafkapoc.tasks.domain.model.Task
import io.jaranas.kafkapoc.tasks.domain.repository.TaskRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoTemplate
import java.time.Instant
import java.util.UUID

/**
 * Integration test for the MongoDB-backed [TaskRepository] implementation.
 *
 * Boots the full Spring context against a real MongoDB and Kafka via Testcontainers
 * (see [IntegrationTestBase]). The Kafka adapter is exercised by the e2e tests; here
 * we focus on the persistence adapter.
 */
@SpringBootTest
class TaskRepositoryImplIntegrationTest : IntegrationTestBase() {

    @Autowired
    private lateinit var taskRepository: TaskRepository

    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    @BeforeEach
    fun cleanCollection() {
        mongoTemplate.dropCollection("tasks")
        mongoTemplate.dropCollection("notifications")
    }

    @Test
    fun `should persist and retrieve a task by id`() {
        // given
        val task = sampleTask()

        // when
        taskRepository.save(task = task)
        val found = taskRepository.findById(id = task.id)

        // then
        assertNotNull(found)
        assertEquals(task.id, found!!.id)
        assertEquals(task.title, found.title)
    }

    @Test
    fun `should return null when finding an unknown task`() {
        // when
        val found = taskRepository.findById(id = UUID.randomUUID())

        // then
        assertNull(found)
    }

    @Test
    fun `should list only non-archived tasks of a user`() {
        // given
        val userId = UUID.randomUUID()
        val anotherUserId = UUID.randomUUID()
        taskRepository.save(task = sampleTask(userId = userId, archived = false))
        taskRepository.save(task = sampleTask(userId = userId, archived = false))
        taskRepository.save(task = sampleTask(userId = userId, archived = true))
        taskRepository.save(task = sampleTask(userId = anotherUserId, archived = false))

        // when
        val result = taskRepository.findByUserIdAndArchivedFalse(userId = userId)

        // then
        assertEquals(2, result.size)
    }

    private fun sampleTask(
        userId: UUID = UUID.randomUUID(),
        archived: Boolean = false,
    ): Task = Task(
        id = UUID.randomUUID(),
        userId = userId,
        title = "title",
        description = "desc",
        completed = false,
        archived = archived,
        createdAt = Instant.parse("2025-01-01T00:00:00Z"),
        updatedAt = Instant.parse("2025-01-01T00:00:00Z"),
    )
}
