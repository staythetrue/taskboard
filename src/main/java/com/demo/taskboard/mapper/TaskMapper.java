package com.demo.taskboard.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.demo.taskboard.domain.Task;
import com.demo.taskboard.domain.TaskStatus;
import com.demo.taskboard.dto.TaskDetailVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * BaseMapper 已提供通用 CRUD。
 * 这里额外声明一个多表关联的分页查询,SQL 写在 resources/mapper/TaskMapper.xml。
 */
@Mapper
public interface TaskMapper extends BaseMapper<Task> {

    /**
     * 任务 + 分类关联分页查询。
     * Page 作为第一个参数时,MyBatis-Plus 的分页插件会自动拼接 LIMIT 并生成 count 语句。
     *
     * @param orderBy 已由服务层白名单校验,只能是固定的几个列名
     */
    IPage<TaskDetailVO> selectTaskPage(Page<TaskDetailVO> page,
                                       @Param("keyword") String keyword,
                                       @Param("status") TaskStatus status,
                                       @Param("categoryId") Long categoryId,
                                       @Param("orderBy") String orderBy);
}
