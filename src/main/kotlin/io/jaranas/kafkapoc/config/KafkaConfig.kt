package io.jaranas.kafkapoc.config

import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.serializer.JacksonJsonSerializer

@Configuration
class KafkaConfig(
    @param:Value($$"${spring.kafka.bootstrap-servers}") private val bootstrapServers: String,
) {

    @Bean
    @ConditionalOnMissingBean
    fun kafkaTemplate(): KafkaTemplate<String, Any> {
        val props = mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JacksonJsonSerializer::class.java,
        )
        val producerFactory = DefaultKafkaProducerFactory<String, Any>(props)
        return KafkaTemplate(producerFactory)
    }
}
