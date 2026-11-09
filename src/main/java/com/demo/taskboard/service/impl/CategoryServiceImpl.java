package com.demo.taskboard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.demo.taskboard.domain.Category;
import com.demo.taskboard.dto.CategoryStatVO;
import com.demo.taskboard.mapper.CategoryMapper;
import com.demo.taskboard.service.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    public CategoryServiceImpl(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> list() {
        return categoryMapper.selectList(
                new LambdaQueryWrapper<Category>().orderByAsc(Category::getSortNo));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryStatVO> stats() {
        return categoryMapper.selectCategoryStats();
    }
}
