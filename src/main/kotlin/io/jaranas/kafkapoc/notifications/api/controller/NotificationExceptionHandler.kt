package io.jaranas.kafkapoc.notifications.api.controller

import io.jaranas.kafkapoc.notifications.domain.exception.NotificationNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class NotificationExceptionHandler {

    @ExceptionHandler(NotificationNotFoundException::class)
    fun handleNotificationNotFound(ex: NotificationNotFoundException): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("error" to (ex.message ?: "Not found")))
}
