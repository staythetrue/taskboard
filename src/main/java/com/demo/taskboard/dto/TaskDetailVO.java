package com.demo.taskboard.dto;

import com.demo.taskboard.domain.Category;
import com.demo.taskboard.domain.TaskStatus;

import java.time.LocalDateTime;

/**
 * 多表关联查询的返回对象。
 * 用嵌套的 Category 演示 resultMap 的 association 映射(而不是把列平铺成 categoryName)。
 */
public class TaskDetailVO {

    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    private Integer priority;
    private LocalDateTime createdAt;
    private Category category;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
}
