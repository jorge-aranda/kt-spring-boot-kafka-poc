package io.jaranas.kafkapoc.tasks.api.mcp

import io.jaranas.kafkapoc.tasks.application.model.TaskRequest
import io.jaranas.kafkapoc.tasks.application.usecase.ArchiveTaskUseCase
import io.jaranas.kafkapoc.tasks.application.usecase.CompleteTaskUseCase
import io.jaranas.kafkapoc.tasks.application.usecase.CreateTaskUseCase
import io.jaranas.kafkapoc.tasks.application.usecase.GetTaskUseCase
import io.jaranas.kafkapoc.tasks.application.usecase.ListUserTasksUseCase
import io.jaranas.kafkapoc.tasks.api.mcp.model.TaskToolResponse
import io.jaranas.kafkapoc.tasks.api.mcp.model.toToolResponse
import org.springframework.ai.tool.annotation.Tool
import org.springframework.ai.tool.annotation.ToolParam
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * MCP (Model Context Protocol) adapter that exposes the tasks application
 * use cases as MCP tools, providing an alternative entry point to the
 * REST [io.jaranas.kafkapoc.tasks.api.controller.TaskController].
 *
 * Since MCP clients do not carry a Spring Security [java.security.Principal],
 * the caller must always provide the `userId` explicitly.
 */
@Component
class TaskMcpTools(
    private val createTaskUseCase: CreateTaskUseCase,
    private val getTaskUseCase: GetTaskUseCase,
    private val listUserTasksUseCase: ListUserTasksUseCase,
    private val completeTaskUseCase: CompleteTaskUseCase,
    private val archiveTaskUseCase: ArchiveTaskUseCase,
) {

    @Tool(
        name = "tasks_create_task",
        description = "Create a new task for a user. Idempotent on taskId: returns the existing task " +
            "if one already exists with the same taskId for the given userId.",
    )
    fun createTask(
        @ToolParam(description = "UUIDv4 of the task to create.") taskId: String,
        @ToolParam(description = "UUIDv4 of the user that owns the task.") userId: String,
        @ToolParam(description = "Short title of the task (max 255 chars).") title: String,
        @ToolParam(description = "Detailed description of the task (max 1000 chars).") description: String,
    ): TaskToolResponse {
        val taskUuid = UUID.fromString(taskId)
        val userUuid = UUID.fromString(userId)
        val existing = runCatching { getTaskUseCase(taskId = taskUuid, userId = userUuid) }.getOrNull()
        if (existing != null) {
            return existing.toToolResponse()
        }
        val request = TaskRequest(
            taskId = taskUuid,
            userId = userUuid,
            title = title,
            description = description,
        )
        return createTaskUseCase(request = request).toToolResponse()
    }

    @Tool(
        name = "tasks_get_task",
        description = "Retrieve a single task by its id for the given user.",
    )
    fun getTask(
        @ToolParam(description = "UUIDv4 of the task.") taskId: String,
        @ToolParam(description = "UUIDv4 of the user that owns the task.") userId: String,
    ): TaskToolResponse =
        getTaskUseCase(
            taskId = UUID.fromString(taskId),
            userId = UUID.fromString(userId),
        ).toToolResponse()

    @Tool(
        name = "tasks_list_user_tasks",
        description = "List all active (non-archived) tasks for the given user.",
    )
    fun listUserTasks(
        @ToolParam(description = "UUIDv4 of the user.") userId: String,
    ): List<TaskToolResponse> =
        listUserTasksUseCase(userId = UUID.fromString(userId)).map { it.toToolResponse() }

    @Tool(
        name = "tasks_complete_task",
        description = "Mark a task as completed for the given user.",
    )
    fun completeTask(
        @ToolParam(description = "UUIDv4 of the task to complete.") taskId: String,
        @ToolParam(description = "UUIDv4 of the user that owns the task.") userId: String,
    ): TaskToolResponse =
        completeTaskUseCase(
            taskId = UUID.fromString(taskId),
            userId = UUID.fromString(userId),
        ).toToolResponse()

    @Tool(
        name = "tasks_archive_task",
        description = "Archive (soft-delete) a task for the given user.",
    )
    fun archiveTask(
        @ToolParam(description = "UUIDv4 of the task to archive.") taskId: String,
        @ToolParam(description = "UUIDv4 of the user that owns the task.") userId: String,
    ): TaskToolResponse =
        archiveTaskUseCase(
            taskId = UUID.fromString(taskId),
            userId = UUID.fromString(userId),
        ).toToolResponse()
}
