package io.jaranas.kafkapoc.notifications.infrastructure.messaging.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.support.converter.ByteArrayJacksonJsonMessageConverter
import org.springframework.kafka.support.converter.RecordMessageConverter

/**
 * Wires a [ByteArrayJacksonJsonMessageConverter] so that the Kafka listener container
 * factory auto-configured by Spring Boot deserialises raw JSON bytes (produced by the
 * `tasks` domain) into the parameter type of each `@KafkaListener` method, without
 * requiring `__TypeId__` headers from the producer.
 *
 * Combined with `value-deserializer: ByteArrayDeserializer` in `application.yml`,
 * this lets the producer and consumer evolve independently per topic.
 */
@Configuration
class KafkaConsumerConfig {

    @Bean
    fun recordMessageConverter(): RecordMessageConverter = ByteArrayJacksonJsonMessageConverter()
}
