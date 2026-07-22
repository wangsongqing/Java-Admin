package com.admin;

import com.admin.common.Result;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 统一响应 Result 的单元测试（不依赖 Spring 上下文）
 */
class ResultTest {

    @Test
    void successWithData() {
        Result<String> result = Result.success("hello");
        assertEquals(200, result.getCode());
        assertEquals("操作成功", result.getMessage());
        assertEquals("hello", result.getData());
    }

    @Test
    void successWithoutData() {
        Result<Void> result = Result.success();
        assertEquals(200, result.getCode());
        assertNull(result.getData());
    }

    @Test
    void successWithMessage() {
        Result<String> result = Result.success("创建成功", "data");
        assertEquals(200, result.getCode());
        assertEquals("创建成功", result.getMessage());
        assertEquals("data", result.getData());
    }

    @Test
    void errorDefaultCode() {
        Result<Void> result = Result.error("出错了");
        assertEquals(500, result.getCode());
        assertEquals("出错了", result.getMessage());
    }

    @Test
    void errorCustomCode() {
        Result<Void> result = Result.error(403, "无权限");
        assertEquals(403, result.getCode());
        assertEquals("无权限", result.getMessage());
    }
}
