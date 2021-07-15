package com.rednuo.avery.exception;

import com.rednuo.core.response.ResultCode;
import lombok.ToString;

/**
 * @author  nz.zou 2021/7/12
 * @since avery 1.0.0
 */
@ToString
public enum  AveryCode implements ResultCode {
    /**
     * 异常代码
     */
    AVERY_ADD_EMPTY_ERROR(false,10040001,"Add data is empty！"),
    AVERY_INPUT_ERROR(false,10040002,"input params error！"),
    AVERY_SAVE_TO_CSV_ERROR(false,10040003,"保存到CSV出错！"),
    AVERY_ERROR(false,10040004,"Avery Error！");

    /**
     * 构造
     */
    AveryCode(boolean success, int code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
    }

    /**
     * 操作代码
     */
    boolean success;
    int code;
    /**
     *     提示信息
     */
    String message;
    @Override
    public boolean isSuccess() {
        return success;
    }

    @Override
    public int code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}
