package io.jaranas.kafkapoc

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(
    properties = [
        "spring.autoconfigure.exclude=" +
            "org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration," +
            "org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration," +
            "org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration," +
            "org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration",
    ],
)
class KafkaPocApplicationTests {

    @Test
    fun contextLoads() {
    }
}
