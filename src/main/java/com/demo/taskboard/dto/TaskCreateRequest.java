package com.demo.taskboard.dto;

import com.demo.taskboard.domain.TaskStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 创建任务的入参。status / priority / categoryId 允许省略,由服务层补默认值。
 */
public record TaskCreateRequest(

        @NotBlank(message = "标题不能为空")
        @Size(max = 200, message = "标题不能超过 200 字")
        String title,

        @Size(max = 1000, message = "描述不能超过 1000 字")
        String description,

        TaskStatus status,

        @Min(value = 0, message = "优先级不能小于 0")
        @Max(value = 5, message = "优先级不能大于 5")
        Integer priority,

        Long categoryId
) {
}
