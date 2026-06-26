package io.jaranas.kafkapoc.tasks.infrastructure.messaging.producer

import io.jaranas.kafkapoc.support.IntegrationTestBase
import io.jaranas.kafkapoc.tasks.domain.event.TaskCreatedEvent
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.Duration
import java.time.Instant
import java.util.UUID

/**
 * Integration test for the Kafka producer adapter [TaskEventKafkaPublisher].
 *
 * Boots the full Spring context against real MongoDB + Kafka (Testcontainers) and
 * subscribes a raw consumer to verify that the publisher writes to the expected topic
 * with the expected record key (taskId).
 */
@SpringBootTest
class TaskEventKafkaPublisherIntegrationTest : IntegrationTestBase() {

    @Autowired
    private lateinit var publisher: TaskEventKafkaPublisher

    @Test
    fun `should publish TaskCreatedEvent on the task-created topic with taskId as key`() {
        // given
        val taskId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val event = TaskCreatedEvent(
            taskId = taskId,
            userId = userId,
            title = "title",
            createdAt = Instant.parse("2025-01-01T00:00:00Z"),
        )

        rawConsumer(topic = TaskEventKafkaPublisher.TOPIC_TASK_CREATED).use { consumer ->
            // when
            publisher.publish(event = event)

            // then
            val records = consumer.poll(Duration.ofSeconds(10))
            val record = records.firstOrNull { it.topic() == TaskEventKafkaPublisher.TOPIC_TASK_CREATED }
            assertNotNull(record)
            assertEquals(taskId.toString(), record!!.key())
        }
    }

    private fun rawConsumer(topic: String): KafkaConsumer<String, String> {
        val props = java.util.Properties().apply {
            put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.bootstrapServers)
            put(ConsumerConfig.GROUP_ID_CONFIG, "it-${UUID.randomUUID()}")
            put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
            put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
            put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer::class.java.name)
        }
        return KafkaConsumer<String, String>(props).apply { subscribe(listOf(topic)) }
    }
}
