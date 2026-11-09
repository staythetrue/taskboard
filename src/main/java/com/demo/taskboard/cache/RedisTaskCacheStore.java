package com.demo.taskboard.cache;

import com.demo.taskboard.dto.TaskResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

/**
 * Redis 实现。采用 Cache-Aside 模式:
 * 读:先查缓存,未命中查库并回填;写:更新库后删除缓存。
 */
@Component
public class RedisTaskCacheStore implements TaskCacheStore {

    private static final Logger log = LoggerFactory.getLogger(RedisTaskCacheStore.class);

    private static final String KEY_PREFIX = "task:detail:";
    private static final String VIEW_PREFIX = "task:view:";
    private static final Duration TTL = Duration.ofMinutes(10);

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;

    public RedisTaskCacheStore(StringRedisTemplate redis, ObjectMapper objectMapper) {
        this.redis = redis;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<TaskResponse> find(long id) {
        try {
            String json = redis.opsForValue().get(KEY_PREFIX + id);
            if (json == null) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(json, TaskResponse.class));
        } catch (Exception ex) {
            // 缓存故障必须降级,不能影响主流程
            log.warn("读取缓存失败,id={}", id, ex);
            return Optional.empty();
        }
    }

    @Override
    public void save(TaskResponse task) {
        try {
            redis.opsForValue().set(KEY_PREFIX + task.id(),
                    objectMapper.writeValueAsString(task), TTL);
        } catch (Exception ex) {
            log.warn("写入缓存失败,id={}", task.id(), ex);
        }
    }

    @Override
    public void evict(long id) {
        try {
            redis.delete(KEY_PREFIX + id);
        } catch (Exception ex) {
            log.warn("删除缓存失败,id={}", id, ex);
        }
    }

    @Override
    public long incrementView(long id) {
        try {
            Long value = redis.opsForValue().increment(VIEW_PREFIX + id);
            return value == null ? 0L : value;
        } catch (Exception ex) {
            log.warn("累加浏览量失败,id={}", id, ex);
            return 0L;
        }
    }

    @Override
    public long viewCount(long id) {
        try {
            String value = redis.opsForValue().get(VIEW_PREFIX + id);
            return value == null ? 0L : Long.parseLong(value);
        } catch (Exception ex) {
            log.warn("读取浏览量失败,id={}", id, ex);
            return 0L;
        }
    }
}
