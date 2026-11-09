package com.demo.taskboard.controller;

import com.demo.taskboard.common.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 根路径导航。直接访问 http://localhost:8080 时给出可用接口清单,避免看到 404/500。
 */
@RestController
public class HomeController {

    @GetMapping("/")
    public ApiResponse<Map<String, Object>> index() {
        return ApiResponse.ok(Map.of(
                "service", "taskboard",
                "version", "1.0.0",
                "health", "/actuator/health",
                "endpoints", List.of(
                        "GET    /api/v1/tasks?keyword=&status=&page=1&size=10",
                        "POST   /api/v1/tasks",
                        "GET    /api/v1/tasks/{id}",
                        "PUT    /api/v1/tasks/{id}",
                        "DELETE /api/v1/tasks/{id}",
                        "GET    /api/v1/tasks/stats",
                        "POST   /api/v1/tasks/{id}/views"
                )
        ));
    }
}
