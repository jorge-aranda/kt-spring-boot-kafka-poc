package io.jaranas.kafkapoc.support

import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.kafka.ConfluentKafkaContainer
import org.testcontainers.utility.DockerImageName

/**
 * Base class for integration tests.
 *
 * Boots a MongoDB and a Kafka container once per JVM (singleton pattern, faster than
 * `@Testcontainers` per-class) and exposes their addresses to Spring through
 * `@DynamicPropertySource`.
 */
abstract class IntegrationTestBase {

    companion object {
        @JvmStatic
        val mongo: MongoDBContainer = MongoDBContainer(DockerImageName.parse("mongo:7.0"))
            .also { it.start() }

        @JvmStatic
        val kafka: ConfluentKafkaContainer = ConfluentKafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.6.1"),
        ).also { it.start() }

        @JvmStatic
        @DynamicPropertySource
        fun registerProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.mongodb.uri") { "${mongo.replicaSetUrl}?uuidRepresentation=standard" }
            registry.add("spring.kafka.bootstrap-servers") { kafka.bootstrapServers }
        }
    }
}
