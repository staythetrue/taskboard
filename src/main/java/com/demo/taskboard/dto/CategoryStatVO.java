package com.demo.taskboard.dto;

/**
 * 分类统计。对应一条 GROUP BY 聚合查询。
 */
public class CategoryStatVO {

    private Long categoryId;
    private String categoryName;
    private Long taskCount;

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Long getTaskCount() {
        return taskCount;
    }

    public void setTaskCount(Long taskCount) {
        this.taskCount = taskCount;
    }
}
