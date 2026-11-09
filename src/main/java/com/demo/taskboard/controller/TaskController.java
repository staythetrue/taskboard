package com.demo.taskboard.controller;

import com.demo.taskboard.common.ApiResponse;
import com.demo.taskboard.common.PageResult;
import com.demo.taskboard.domain.TaskStatus;
import com.demo.taskboard.dto.TaskCreateRequest;
import com.demo.taskboard.dto.TaskDetailVO;
import com.demo.taskboard.dto.TaskResponse;
import com.demo.taskboard.dto.TaskStats;
import com.demo.taskboard.dto.TaskUpdateRequest;
import com.demo.taskboard.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<TaskResponse> create(@Valid @RequestBody TaskCreateRequest request) {
        return ApiResponse.ok(taskService.create(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<TaskResponse> get(@PathVariable long id) {
        return ApiResponse.ok(taskService.get(id));
    }

    @GetMapping
    public ApiResponse<PageResult<TaskResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(taskService.search(keyword, status, page, size));
    }

    @GetMapping("/with-category")
    public ApiResponse<PageResult<TaskDetailVO>> searchWithCategory(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(
                taskService.searchDetail(keyword, status, categoryId, sort, direction, page, size));
    }

    @PutMapping("/{id}")
    public ApiResponse<TaskResponse> update(@PathVariable long id,
                                            @Valid @RequestBody TaskUpdateRequest request) {
        return ApiResponse.ok(taskService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable long id) {
        taskService.delete(id);
        return ApiResponse.ok();
    }

    @GetMapping("/stats")
    public ApiResponse<TaskStats> stats() {
        return ApiResponse.ok(taskService.stats());
    }

    @PostMapping("/{id}/views")
    public ApiResponse<Long> recordView(@PathVariable long id) {
        return ApiResponse.ok(taskService.recordView(id));
    }
}
