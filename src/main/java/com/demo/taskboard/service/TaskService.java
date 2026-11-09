package com.demo.taskboard.service;

import com.demo.taskboard.common.PageResult;
import com.demo.taskboard.domain.TaskStatus;
import com.demo.taskboard.dto.TaskCreateRequest;
import com.demo.taskboard.dto.TaskDetailVO;
import com.demo.taskboard.dto.TaskResponse;
import com.demo.taskboard.dto.TaskStats;
import com.demo.taskboard.dto.TaskUpdateRequest;

public interface TaskService {

    TaskResponse create(TaskCreateRequest request);

    TaskResponse get(long id);

    PageResult<TaskResponse> search(String keyword, TaskStatus status, int page, int size);

    /**
     * 关联分类的分页查询(自定义 SQL + resultMap 嵌套映射)。
     *
     * @param sort      排序列,只允许 id / created_at / priority
     * @param direction asc 或 desc
     */
    PageResult<TaskDetailVO> searchDetail(String keyword, TaskStatus status, Long categoryId,
                                          String sort, String direction, int page, int size);

    TaskResponse update(long id, TaskUpdateRequest request);

    void delete(long id);

    TaskStats stats();

    long recordView(long id);
}
