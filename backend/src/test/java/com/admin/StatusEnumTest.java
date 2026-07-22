package com.admin;

import com.admin.enums.StatusEnum;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 状态枚举的单元测试（不依赖 Spring 上下文）
 */
class StatusEnumTest {

    @Test
    void isNormal() {
        assertTrue(StatusEnum.isNormal(1));
        assertFalse(StatusEnum.isNormal(0));
        assertFalse(StatusEnum.isNormal(null));
    }

    @Test
    void isDisabled() {
        assertTrue(StatusEnum.isDisabled(0));
        assertFalse(StatusEnum.isDisabled(1));
        assertFalse(StatusEnum.isDisabled(null));
    }

    @Test
    void codeAndDesc() {
        assertEquals(0, StatusEnum.DISABLED.getCode());
        assertEquals("禁用", StatusEnum.DISABLED.getDesc());
        assertEquals(1, StatusEnum.NORMAL.getCode());
        assertEquals("正常", StatusEnum.NORMAL.getDesc());
    }
}
