package com.demo.taskboard.cache;

import com.demo.taskboard.dto.TaskResponse;

import java.util.Optional;

/**
 * 任务缓存抽象。
 * 面向接口编程:服务层不关心底层是 Redis 还是本地 Map,测试时可直接替换。
 */
public interface TaskCacheStore {

    Optional<TaskResponse> find(long id);

    void save(TaskResponse task);

    void evict(long id);

    long incrementView(long id);

    long viewCount(long id);
}
