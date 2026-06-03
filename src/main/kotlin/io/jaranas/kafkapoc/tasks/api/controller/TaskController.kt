package io.jaranas.kafkapoc.tasks.api.controller

import io.jaranas.kafkapoc.tasks.api.model.CreateTaskRequestDto
import io.jaranas.kafkapoc.tasks.api.model.TaskResponseDto
import io.jaranas.kafkapoc.tasks.api.model.toResponseDto
import io.jaranas.kafkapoc.tasks.application.model.TaskRequest
import io.jaranas.kafkapoc.tasks.application.usecase.ArchiveTaskUseCase
import io.jaranas.kafkapoc.tasks.application.usecase.CompleteTaskUseCase
import io.jaranas.kafkapoc.tasks.application.usecase.CreateTaskUseCase
import io.jaranas.kafkapoc.tasks.application.usecase.GetTaskUseCase
import io.jaranas.kafkapoc.tasks.application.usecase.ListUserTasksUseCase
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.Principal

@RestController
@RequestMapping("/api/tasks")
class TaskController(
    private val createTaskUseCase: CreateTaskUseCase,
    private val getTaskUseCase: GetTaskUseCase,
    private val listUserTasksUseCase: ListUserTasksUseCase,
    private val completeTaskUseCase: CompleteTaskUseCase,
    private val archiveTaskUseCase: ArchiveTaskUseCase,
) {

    @PutMapping("/{taskId}")
    fun createTask(
        @PathVariable taskId: String,
        @Valid @RequestBody body: CreateTaskRequestDto,
        principal: Principal,
    ): ResponseEntity<TaskResponseDto> {
        val userId = principal.name
        val existing = runCatching { getTaskUseCase.execute(taskId = taskId, userId = userId) }.getOrNull()
        return if (existing != null) {
            ResponseEntity.ok(existing.toResponseDto())
        } else {
            val request = TaskRequest(
                taskId = taskId,
                userId = userId,
                title = body.title,
                description = body.description,
            )
            val created = createTaskUseCase.execute(request = request)
            ResponseEntity.status(HttpStatus.CREATED).body(created.toResponseDto())
        }
    }

    @GetMapping
    fun listTasks(principal: Principal): ResponseEntity<List<TaskResponseDto>> {
        val tasks = listUserTasksUseCase.execute(userId = principal.name)
        return ResponseEntity.ok(tasks.map { it.toResponseDto() })
    }

    @GetMapping("/{taskId}")
    fun getTask(
        @PathVariable taskId: String,
        principal: Principal,
    ): ResponseEntity<TaskResponseDto> {
        val task = getTaskUseCase.execute(taskId = taskId, userId = principal.name)
        return ResponseEntity.ok(task.toResponseDto())
    }

    @PatchMapping("/{taskId}/complete")
    fun completeTask(
        @PathVariable taskId: String,
        principal: Principal,
    ): ResponseEntity<TaskResponseDto> {
        val task = completeTaskUseCase.execute(taskId = taskId, userId = principal.name)
        return ResponseEntity.ok(task.toResponseDto())
    }

    @DeleteMapping("/{taskId}")
    fun archiveTask(
        @PathVariable taskId: String,
        principal: Principal,
    ): ResponseEntity<Void> {
        archiveTaskUseCase.execute(taskId = taskId, userId = principal.name)
        return ResponseEntity.noContent().build()
    }
}
