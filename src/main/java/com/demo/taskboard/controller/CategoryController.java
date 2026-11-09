package com.demo.taskboard.controller;

import com.demo.taskboard.common.ApiResponse;
import com.demo.taskboard.domain.Category;
import com.demo.taskboard.dto.CategoryStatVO;
import com.demo.taskboard.service.CategoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ApiResponse<List<Category>> list() {
        return ApiResponse.ok(categoryService.list());
    }

    @GetMapping("/stats")
    public ApiResponse<List<CategoryStatVO>> stats() {
        return ApiResponse.ok(categoryService.stats());
    }
}
