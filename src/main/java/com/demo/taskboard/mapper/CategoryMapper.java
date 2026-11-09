package com.demo.taskboard.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.demo.taskboard.domain.Category;
import com.demo.taskboard.dto.CategoryStatVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    /**
     * 每个分类下的任务数量(GROUP BY + LEFT JOIN,没有任务的分类也要出现)。
     */
    List<CategoryStatVO> selectCategoryStats();
}
