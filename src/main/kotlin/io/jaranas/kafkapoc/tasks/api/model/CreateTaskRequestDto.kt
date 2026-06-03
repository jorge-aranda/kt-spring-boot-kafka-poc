package io.jaranas.kafkapoc.tasks.api.model

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateTaskRequestDto(
    @field:NotBlank
    @field:Size(max = 255)
    val title: String,

    @field:NotBlank
    @field:Size(max = 1000)
    val description: String,
)
