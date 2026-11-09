package com.demo.taskboard.controller;

import com.demo.taskboard.common.BusinessException;
import com.demo.taskboard.common.ErrorCode;
import com.demo.taskboard.domain.TaskStatus;
import com.demo.taskboard.dto.TaskResponse;
import com.demo.taskboard.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    @Test
    @DisplayName("创建任务成功返回 201 与统一响应体")
    void createReturnsCreated() throws Exception {
        TaskResponse response = new TaskResponse(1L, "写代码", null, TaskStatus.TODO, 0, null, null);
        when(taskService.create(any())).thenReturn(response);

        String body = objectMapper.writeValueAsString(Map.of("title", "写代码"));

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.title").value("写代码"));
    }

    @Test
    @DisplayName("标题为空应返回 400 与参数校验错误码")
    void createWithBlankTitleReturnsBadRequest() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("title", "   "));

        mockMvc.perform(post("/api/v1/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.BAD_REQUEST.getCode()));
    }

    @Test
    @DisplayName("查询不存在的任务应返回 404")
    void getMissingReturnsNotFound() throws Exception {
        when(taskService.get(eq(99L)))
                .thenThrow(new BusinessException(ErrorCode.NOT_FOUND, "任务不存在: 99"));

        mockMvc.perform(get("/api/v1/tasks/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.NOT_FOUND.getCode()));
    }
}
