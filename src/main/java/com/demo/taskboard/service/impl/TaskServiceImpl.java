package com.demo.taskboard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.demo.taskboard.cache.TaskCacheStore;
import com.demo.taskboard.common.BusinessException;
import com.demo.taskboard.common.ErrorCode;
import com.demo.taskboard.common.PageResult;
import com.demo.taskboard.domain.Task;
import com.demo.taskboard.domain.TaskStatus;
import com.demo.taskboard.dto.TaskCreateRequest;
import com.demo.taskboard.dto.TaskDetailVO;
import com.demo.taskboard.dto.TaskResponse;
import com.demo.taskboard.dto.TaskStats;
import com.demo.taskboard.dto.TaskUpdateRequest;
import com.demo.taskboard.mapper.TaskMapper;
import com.demo.taskboard.service.TaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;

@Service
public class TaskServiceImpl implements TaskService {

    private static final int MAX_PAGE_SIZE = 100;

    /**
     * 排序白名单。SQL 里的 ${orderBy} 是字符串拼接,只能接受这里列出的列名。
     */
    private static final Set<String> SORTABLE_COLUMNS = Set.of("id", "created_at", "priority");

    private final TaskMapper taskMapper;
    private final TaskCacheStore cache;

    public TaskServiceImpl(TaskMapper taskMapper, TaskCacheStore cache) {
        this.taskMapper = taskMapper;
        this.cache = cache;
    }

    @Override
    @Transactional
    public TaskResponse create(TaskCreateRequest request) {
        Task task = new Task();
        task.setTitle(request.title().trim());
        task.setDescription(request.description());
        task.setStatus(request.status() == null ? TaskStatus.TODO : request.status());
        task.setPriority(request.priority() == null ? 0 : request.priority());
        task.setCategoryId(request.categoryId());

        taskMapper.insert(task);
        return TaskResponse.from(task);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse get(long id) {
        return cache.find(id).orElseGet(() -> {
            Task task = taskMapper.selectById(id);
            if (task == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND, "任务不存在: " + id);
            }
            TaskResponse response = TaskResponse.from(task);
            cache.save(response);
            return response;
        });
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<TaskResponse> search(String keyword, TaskStatus status, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);

        LambdaQueryWrapper<Task> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Task::getTitle, keyword.trim());
        }
        if (status != null) {
            wrapper.eq(Task::getStatus, status);
        }
        wrapper.orderByDesc(Task::getId);

        Page<Task> result = taskMapper.selectPage(new Page<>(safePage, safeSize), wrapper);
        List<TaskResponse> items = result.getRecords().stream().map(TaskResponse::from).toList();
        return PageResult.of(items, result.getTotal(), safePage, safeSize);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<TaskDetailVO> searchDetail(String keyword, TaskStatus status, Long categoryId,
                                                 String sort, String direction, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);

        IPage<TaskDetailVO> result = taskMapper.selectTaskPage(
                new Page<>(safePage, safeSize),
                StringUtils.hasText(keyword) ? keyword.trim() : null,
                status,
                categoryId,
                buildOrderBy(sort, direction));

        return PageResult.of(result.getRecords(), result.getTotal(), safePage, safeSize);
    }

    /**
     * 方向只认 asc/desc,列名只认白名单,其余一律回退到 id desc。
     * 这样即使调用方传了恶意字符串,也拼不进 SQL。
     */
    private String buildOrderBy(String sort, String direction) {
        String column = (sort == null ? "" : sort.trim().toLowerCase());
        if (!SORTABLE_COLUMNS.contains(column)) {
            column = "id";
        }
        String dir = "asc".equalsIgnoreCase(direction) ? "ASC" : "DESC";
        return column + " " + dir;
    }

    @Override
    @Transactional
    public TaskResponse update(long id, TaskUpdateRequest request) {
        Task task = taskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "任务不存在: " + id);
        }

        task.setTitle(request.title().trim());
        if (request.description() != null) {
            task.setDescription(request.description());
        }
        if (request.status() != null) {
            task.setStatus(request.status());
        }
        if (request.priority() != null) {
            task.setPriority(request.priority());
        }
        if (request.categoryId() != null) {
            task.setCategoryId(request.categoryId());
        }

        taskMapper.updateById(task);
        cache.evict(id);
        return TaskResponse.from(task);
    }

    @Override
    @Transactional
    public void delete(long id) {
        Task task = taskMapper.selectById(id);
        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "任务不存在: " + id);
        }
        taskMapper.deleteById(id);
        cache.evict(id);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskStats stats() {
        long total = taskMapper.selectCount(null);
        long todo = taskMapper.selectCount(
                new LambdaQueryWrapper<Task>().eq(Task::getStatus, TaskStatus.TODO));
        long doing = taskMapper.selectCount(
                new LambdaQueryWrapper<Task>().eq(Task::getStatus, TaskStatus.DOING));
        long done = taskMapper.selectCount(
                new LambdaQueryWrapper<Task>().eq(Task::getStatus, TaskStatus.DONE));
        return new TaskStats(total, todo, doing, done);
    }

    @Override
    public long recordView(long id) {
        // 浏览量走 Redis 计数器,不落库,演示独立于数据库的高频写场景
        return cache.incrementView(id);
    }
}
