package com.demo.taskboard.dto;

import com.demo.taskboard.domain.Task;
import com.demo.taskboard.domain.TaskStatus;

import java.time.LocalDateTime;

/**
 * 对外响应体。与实体解耦,避免把 JPA 的内部结构暴露出去。
 */
public record TaskResponse(
        Long id,
        String title,
        String description,
        TaskStatus status,
        Integer priority,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static TaskResponse from(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getPriority(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
