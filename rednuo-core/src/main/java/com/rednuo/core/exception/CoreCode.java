package com.rednuo.core.exception;

import com.rednuo.core.response.ResultCode;
import lombok.ToString;

/**
 * @author  rednuo 2021/4/28
 */
@ToString
public enum CoreCode implements ResultCode{
    //未知异常
    INVALID_PARAM(false, 10003, "非法参数!"),
    INVALID_DATE(false, 10004, "日期格式化!"),
    INVALID_SQL(false, 10005, "SQL执行异常!"),
    FAIL_VALIDATION(false, 10006, "效验失败!"),
    WARN_PERFORMANCE_ISSUE(false, 10007, "潜在的性能问题!"),
    FAIL_INVALID_PARAM(false, 10008, "参数id类型不匹配!"),
    //已知响应
    SUCCESS(true, 10000, "操作成功!"),
    FAIL(false, 11111, "操作失败!"),
    UNAUTHENTICATED(false, 10001, "此操作需要登录系统!"),
    UNAUTHORISE(false, 10002, "权限不足!"),
    SERVER_ERROR(false, 99999, "抱歉,服务繁忙,稍后重试!");
    /**
     * 操作成功
     */
    boolean success;
    /**
     * 操作代码Code
     */
    int code;
    /**
     * 提示信息
     */
    String message;

    private CoreCode(boolean success, int code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
    }

    @Override
    public boolean isSuccess() {
        return this.success;
    }

    @Override
    public int code() {
        return this.code;
    }

    @Override
    public String message() {
        return this.message;
    }
}
