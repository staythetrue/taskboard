package com.demo.taskboard.service;

import com.demo.taskboard.cache.TaskCacheStore;
import com.demo.taskboard.common.BusinessException;
import com.demo.taskboard.domain.Task;
import com.demo.taskboard.domain.TaskStatus;
import com.demo.taskboard.dto.TaskCreateRequest;
import com.demo.taskboard.dto.TaskResponse;
import com.demo.taskboard.dto.TaskUpdateRequest;
import com.demo.taskboard.mapper.TaskMapper;
import com.demo.taskboard.service.impl.TaskServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private TaskCacheStore cache;

    private TaskServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new TaskServiceImpl(taskMapper, cache);
    }

    @Test
    @DisplayName("创建任务时,未传状态和优先级应补默认值")
    void createAppliesDefaults() {
        when(taskMapper.insert(any(Task.class))).thenReturn(1);

        service.create(new TaskCreateRequest("  写单元测试  ", null, null, null, null));

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskMapper).insert(captor.capture());
        Task saved = captor.getValue();
        assertThat(saved.getTitle()).isEqualTo("写单元测试");
        assertThat(saved.getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(saved.getPriority()).isZero();
    }

    @Test
    @DisplayName("缓存命中时不应查询数据库")
    void getUsesCacheFirst() {
        TaskResponse cached = new TaskResponse(1L, "标题", null, TaskStatus.TODO, 0, null, null);
        when(cache.find(1L)).thenReturn(Optional.of(cached));

        TaskResponse result = service.get(1L);

        assertThat(result).isEqualTo(cached);
        verify(taskMapper, never()).selectById(any());
    }

    @Test
    @DisplayName("任务不存在时应抛出 BusinessException")
    void getThrowsWhenMissing() {
        when(cache.find(99L)).thenReturn(Optional.empty());
        when(taskMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> service.get(99L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("99");
    }

    @Test
    @DisplayName("更新后应删除缓存,保证下一次读到新数据")
    void updateEvictsCache() {
        Task task = new Task();
        task.setTitle("旧标题");
        when(taskMapper.selectById(1L)).thenReturn(task);
        when(taskMapper.updateById(any(Task.class))).thenReturn(1);

        service.update(1L, new TaskUpdateRequest("新标题", null, TaskStatus.DONE, 3, null));

        verify(cache).evict(1L);
        assertThat(task.getTitle()).isEqualTo("新标题");
        assertThat(task.getStatus()).isEqualTo(TaskStatus.DONE);
    }

    @Test
    @DisplayName("删除不存在的任务应报错,且不调用删除")
    void deleteMissingTask() {
        when(taskMapper.selectById(7L)).thenReturn(null);

        assertThatThrownBy(() -> service.delete(7L))
                .isInstanceOf(BusinessException.class);
        verify(taskMapper, never()).deleteById(any(java.io.Serializable.class));
    }
}
