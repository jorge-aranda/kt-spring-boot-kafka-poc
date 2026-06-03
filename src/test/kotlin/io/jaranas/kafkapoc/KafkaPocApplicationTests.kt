package io.jaranas.kafkapoc

import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.KafkaTemplate

@SpringBootTest(
    properties = [
        "spring.autoconfigure.exclude=" +
            "org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration," +
            "org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration," +
            "org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration," +
            "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration",
        "spring.kafka.bootstrap-servers=localhost:9092",
    ],
)
class KafkaPocApplicationTests {

    @Configuration
    class TestKafkaConfig {
        @Bean
        fun kafkaTemplate(): KafkaTemplate<String, Any> = mockk(relaxed = true)
    }

    @Test
    fun contextLoads() {
    }
}
