package com.rednuo.core.exception;

import com.rednuo.core.response.ResultCode;

/**
 * 通用的异常处理
 * (json形式返回值同JsonResult，便于前端统一处理)
 * @author  rednuo 2021/4/28
 */
public class CustomException extends RuntimeException {
    private ResultCode resultCode;
    private String msg;

    public ResultCode getResultCode(){
        return resultCode;
    }

    private String msg(){
        return msg;
    }

    public CustomException(ResultCode resultCode){
        // 异常代码+信息
        super("rednuoException code:" + resultCode.code() + " info:" + resultCode.message());
        this.resultCode = resultCode;
    }

    public CustomException(ResultCode resultCode, String msg){
        // 异常代码+信息
        super("rednuoException code:" + resultCode.code() + " info:" + resultCode.message() + " Client explain:" + msg);
        this.resultCode = resultCode;
        this.msg = msg;
    }
}
