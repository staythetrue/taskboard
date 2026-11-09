package com.demo.taskboard.service;

import com.demo.taskboard.domain.Category;
import com.demo.taskboard.dto.CategoryStatVO;

import java.util.List;

public interface CategoryService {

    List<Category> list();

    List<CategoryStatVO> stats();
}
