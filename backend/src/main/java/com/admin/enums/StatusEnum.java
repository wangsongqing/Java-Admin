package com.admin.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通用状态枚举
 *
 * <p>用于 user / role / permission 等实体的启用-禁用状态，
 * 替代散落在业务代码中的魔法值 0 / 1。</p>
 */
@Getter
@AllArgsConstructor
public enum StatusEnum {

    DISABLED(0, "禁用"),
    NORMAL(1, "正常");

    private final Integer code;
    private final String desc;

    /**
     * 判断给定状态码是否为正常
     */
    public static boolean isNormal(Integer code) {
        return NORMAL.code.equals(code);
    }

    /**
     * 判断给定状态码是否为禁用
     */
    public static boolean isDisabled(Integer code) {
        return DISABLED.code.equals(code);
    }
}
