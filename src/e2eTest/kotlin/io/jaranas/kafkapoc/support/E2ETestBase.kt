package io.jaranas.kafkapoc.support

import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.kafka.ConfluentKafkaContainer
import org.testcontainers.utility.DockerImageName

/**
 * Base class for end-to-end tests.
 *
 * Boots a MongoDB and a Kafka container once per JVM and wires their addresses into
 * the Spring environment. Subclasses typically annotate themselves with
 * `@SpringBootTest(webEnvironment = RANDOM_PORT)` and use [org.springframework.boot.test.web.client.TestRestTemplate]
 * to drive the public HTTP API.
 */
abstract class E2ETestBase {

    companion object {
        const val DEV_USERNAME: String = "00000000-0000-0000-0000-000000000001"
        const val DEV_PASSWORD: String = "dev"

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
            registry.add("app.security.dev-user.id") { DEV_USERNAME }
        }
    }
}
