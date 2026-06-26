package io.jaranas.kafkapoc.support

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.web.client.RestClient

/**
 * Tiny HTTP helper used by the e2e tests.
 *
 * Spring Boot 4 no longer ships `TestRestTemplate`, so we wrap a plain [RestClient]
 * pointing at `http://localhost:<port>` and adding HTTP Basic credentials for the
 * configured dev user on every call. We deliberately do NOT throw on 4xx/5xx so
 * that tests can assert error status codes.
 */
class E2EHttpClient(
    port: Int,
    private val username: String,
    private val password: String,
) {

    private val client: RestClient = RestClient.builder()
        .baseUrl("http://localhost:$port")
        .defaultStatusHandler({ true }, { _, _ -> /* never throw, tests inspect status */ })
        .build()

    fun get(path: String): Response =
        client.get().uri(path).headers { it.setBasicAuth(username, password) }.retrieve().toEntity(String::class.java)
            .let { Response(status = it.statusCode, body = it.body, headers = it.headers) }

    fun getAnonymous(path: String): Response =
        client.get().uri(path).retrieve().toEntity(String::class.java)
            .let { Response(status = it.statusCode, body = it.body, headers = it.headers) }

    fun put(path: String, jsonBody: String): Response =
        client.put().uri(path)
            .headers {
                it.setBasicAuth(username, password)
                it.contentType = MediaType.APPLICATION_JSON
            }
            .body(jsonBody)
            .retrieve()
            .toEntity(String::class.java)
            .let { Response(status = it.statusCode, body = it.body, headers = it.headers) }

    fun patch(path: String): Response =
        client.patch().uri(path).headers { it.setBasicAuth(username, password) }.retrieve()
            .toEntity(String::class.java)
            .let { Response(status = it.statusCode, body = it.body, headers = it.headers) }

    fun delete(path: String): Response =
        client.delete().uri(path).headers { it.setBasicAuth(username, password) }.retrieve()
            .toEntity(String::class.java)
            .let { Response(status = it.statusCode, body = it.body, headers = it.headers) }

    data class Response(
        val status: HttpStatusCode,
        val body: String?,
        val headers: HttpHeaders,
    )
}
