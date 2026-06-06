package io.jaranas.kafkapoc.tasks.api.mcp

import org.springframework.ai.tool.ToolCallbackProvider
import org.springframework.ai.tool.method.MethodToolCallbackProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Registers the [TaskMcpTools] instance as a [ToolCallbackProvider] so that
 * Spring AI's MCP server auto-configuration exposes all `@Tool` methods to
 * MCP clients through the configured transport (WebMVC SSE).
 */
@Configuration
class TaskMcpConfig {

    @Bean
    fun taskMcpToolCallbackProvider(taskMcpTools: TaskMcpTools): ToolCallbackProvider =
        MethodToolCallbackProvider.builder()
            .toolObjects(taskMcpTools)
            .build()
}
