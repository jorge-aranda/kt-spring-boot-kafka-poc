package io.jaranas.kafkapoc.tasks

import io.jaranas.kafkapoc.support.E2EHttpClient
import io.jaranas.kafkapoc.support.E2ETestBase
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.http.HttpStatus
import tools.jackson.databind.JsonNode
import tools.jackson.databind.json.JsonMapper
import java.util.UUID

/**
 * End-to-end tests for the `tasks` REST API.
 *
 * Boots the full Spring Boot application against MongoDB + Kafka via Testcontainers
 * and drives the public HTTP API with HTTP Basic credentials matching the configured
 * dev user.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class TasksApiE2ETest : E2ETestBase() {

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    private val json = JsonMapper.builder().build()
    private lateinit var http: E2EHttpClient

    @BeforeEach
    fun setUp() {
        mongoTemplate.dropCollection("tasks")
        mongoTemplate.dropCollection("notifications")
        http = E2EHttpClient(port = port, username = DEV_USERNAME, password = DEV_PASSWORD)
    }

    // -- Happy paths ---------------------------------------------------------

    @Test
    fun `should create a task with PUT and return it on GET`() {
        // given
        val taskId = UUID.randomUUID()

        // when
        val created = http.put(
            path = "/api/tasks/$taskId",
            jsonBody = """{"title":"Buy milk","description":"2L semi-skimmed"}""",
        )

        // then
        assertEquals(HttpStatus.CREATED, created.status)
        val createdNode = json.readTree(created.body)
        assertEquals(taskId.toString(), createdNode["id"].asString())
        assertEquals("Buy milk", createdNode["title"].asString())
        assertEquals(false, createdNode["completed"].asBoolean())

        val fetched = http.get(path = "/api/tasks/$taskId")
        assertEquals(HttpStatus.OK, fetched.status)
        assertEquals(taskId.toString(), json.readTree(fetched.body)["id"].asString())
    }

    @Test
    fun `should be idempotent on PUT with same taskId`() {
        // given
        val taskId = UUID.randomUUID()
        val body = """{"title":"Task","description":"desc"}"""
        http.put(path = "/api/tasks/$taskId", jsonBody = body)

        // when
        val second = http.put(path = "/api/tasks/$taskId", jsonBody = body)

        // then
        assertEquals(HttpStatus.OK, second.status)
        assertEquals(taskId.toString(), json.readTree(second.body)["id"].asString())
    }

    @Test
    fun `should complete and archive a task`() {
        // given
        val taskId = UUID.randomUUID()
        http.put(path = "/api/tasks/$taskId", jsonBody = """{"title":"t","description":"d"}""")

        // when: complete
        val completed = http.patch(path = "/api/tasks/$taskId/complete")

        // then
        assertEquals(HttpStatus.OK, completed.status)
        assertEquals(true, json.readTree(completed.body)["completed"].asBoolean())

        // when: archive
        val archived = http.delete(path = "/api/tasks/$taskId")

        // then
        assertEquals(HttpStatus.NO_CONTENT, archived.status)
    }

    @Test
    fun `should list only the authenticated user non-archived tasks`() {
        // given
        val activeId = UUID.randomUUID()
        val archivedId = UUID.randomUUID()
        http.put(path = "/api/tasks/$activeId", jsonBody = """{"title":"active","description":"d"}""")
        http.put(path = "/api/tasks/$archivedId", jsonBody = """{"title":"to-archive","description":"d"}""")
        http.delete(path = "/api/tasks/$archivedId")

        // when
        val response = http.get(path = "/api/tasks")

        // then
        assertEquals(HttpStatus.OK, response.status)
        val ids: List<String> = json.readTree(response.body).values().map { it["id"].asString() }
        assertTrue(activeId.toString() in ids)
        assertTrue(archivedId.toString() !in ids)
    }

    // -- Errors --------------------------------------------------------------

    @Test
    fun `should return 401 when no credentials are provided`() {
        // when
        val response = http.getAnonymous(path = "/api/tasks")

        // then
        assertEquals(HttpStatus.UNAUTHORIZED, response.status)
    }

    @Test
    fun `should return 404 when getting an unknown task`() {
        // when
        val response = http.get(path = "/api/tasks/${UUID.randomUUID()}")

        // then
        assertEquals(HttpStatus.NOT_FOUND, response.status)
    }

    @Test
    fun `should return 400 when creating a task with blank title`() {
        // when
        val response = http.put(
            path = "/api/tasks/${UUID.randomUUID()}",
            jsonBody = """{"title":"","description":"desc"}""",
        )

        // then
        assertEquals(HttpStatus.BAD_REQUEST, response.status)
        assertNotNull(json.readTree(response.body)["title"])
    }

    @Test
    fun `should return 404 when completing an unknown task`() {
        // when
        val response = http.patch(path = "/api/tasks/${UUID.randomUUID()}/complete")

        // then
        assertEquals(HttpStatus.NOT_FOUND, response.status)
    }
}
