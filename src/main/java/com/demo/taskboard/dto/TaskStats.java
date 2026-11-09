package com.demo.taskboard.dto;

/**
 * 任务统计。用于演示聚合查询 + 缓存。
 */
public record TaskStats(long total, long todo, long doing, long done) {
}
