package io.jaranas.kafkapoc

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class KafkaPocApplication

fun main(args: Array<String>) {
    runApplication<KafkaPocApplication>(*args)
}
