package io.jaranas.kafkapoc.notifications

import io.jaranas.kafkapoc.support.E2EHttpClient
import io.jaranas.kafkapoc.support.E2ETestBase
import org.awaitility.Awaitility
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
import tools.jackson.databind.json.JsonMapper
import java.time.Duration
import java.util.UUID

/**
 * End-to-end tests for the `notifications` REST API and for the cross-domain
 * Kafka pipeline `tasks -> notifications`.
 *
 * Creating a task via the public `tasks` API publishes `tasks.task-created.v1` on
 * Kafka; the `notifications` consumer materialises a `Notification` document on
 * MongoDB, which we then expose through the `notifications` API.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class NotificationsApiE2ETest : E2ETestBase() {

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

    // -- Cross-domain Kafka pipeline ----------------------------------------

    @Test
    fun `should materialise a notification when a task is created (Kafka pipeline)`() {
        // given
        val taskId = UUID.randomUUID()

        // when
        val created = http.put(
            path = "/api/tasks/$taskId",
            jsonBody = """{"title":"My task","description":"desc"}""",
        )
        assertEquals(HttpStatus.CREATED, created.status)

        // then: eventually the notification appears via Kafka -> consumer -> Mongo
        Awaitility.await()
            .atMost(Duration.ofSeconds(20))
            .pollInterval(Duration.ofMillis(500))
            .untilAsserted {
                val response = http.get(path = "/api/notifications")
                assertEquals(HttpStatus.OK, response.status)
                val match = json.readTree(response.body).values()
                    .firstOrNull { it["taskId"].asString() == taskId.toString() }
                assertNotNull(match, "expected a notification for taskId=$taskId")
                assertEquals("TASK_CREATED", match!!["type"].asString())
                assertEquals(false, match["read"].asBoolean())
            }
    }

    // -- Notifications API ---------------------------------------------------

    @Test
    fun `should mark a notification as read and then archive it`() {
        // given
        val taskId = UUID.randomUUID()
        http.put(path = "/api/tasks/$taskId", jsonBody = """{"title":"t","description":"d"}""")
        val notificationId = awaitNotificationIdForTask(taskId = taskId)

        // when: mark as read
        val read = http.patch(path = "/api/notifications/$notificationId/read")

        // then
        assertEquals(HttpStatus.OK, read.status)
        assertEquals(true, json.readTree(read.body)["read"].asBoolean())

        // when: archive
        val archived = http.delete(path = "/api/notifications/$notificationId")

        // then
        assertEquals(HttpStatus.NO_CONTENT, archived.status)

        val list = http.get(path = "/api/notifications")
        val ids = json.readTree(list.body).values().map { it["id"].asString() }
        assertTrue(notificationId.toString() !in ids)
    }

    // -- Errors --------------------------------------------------------------

    @Test
    fun `should return 401 when listing notifications without credentials`() {
        // when
        val response = http.getAnonymous(path = "/api/notifications")

        // then
        assertEquals(HttpStatus.UNAUTHORIZED, response.status)
    }

    @Test
    fun `should return 404 when getting an unknown notification`() {
        // when
        val response = http.get(path = "/api/notifications/${UUID.randomUUID()}")

        // then
        assertEquals(HttpStatus.NOT_FOUND, response.status)
    }

    @Test
    fun `should return 404 when marking an unknown notification as read`() {
        // when
        val response = http.patch(path = "/api/notifications/${UUID.randomUUID()}/read")

        // then
        assertEquals(HttpStatus.NOT_FOUND, response.status)
    }

    // -- Helpers -------------------------------------------------------------

    private fun awaitNotificationIdForTask(taskId: UUID): UUID {
        var id: UUID? = null
        Awaitility.await()
            .atMost(Duration.ofSeconds(20))
            .pollInterval(Duration.ofMillis(500))
            .untilAsserted {
                val response = http.get(path = "/api/notifications")
                val match = json.readTree(response.body).values()
                    .firstOrNull { it["taskId"].asString() == taskId.toString() }
                assertNotNull(match)
                id = UUID.fromString(match!!["id"].asString())
            }
        return id!!
    }
}
