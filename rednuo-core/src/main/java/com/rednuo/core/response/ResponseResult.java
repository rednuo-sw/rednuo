package com.rednuo.core.response;

import com.rednuo.core.exception.CoreCode;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 响应数据的格式
 * @author rednuo 2020/8/17
 */
@Data
@ToString
@NoArgsConstructor
public class ResponseResult<T> {
    boolean isSuccess = true;
    int code = 10000;
    String message;
    long timestamp = System.currentTimeMillis();
    T result;

    public ResponseResult(ResultCode resultCode) {
        this.isSuccess=resultCode.isSuccess();
        this.code=resultCode.code();
        this.message=resultCode.message();
    }

    public ResponseResult(ResultCode resultCode, T data) {
        this.isSuccess=resultCode.isSuccess();
        this.code=resultCode.code();
        this.message=resultCode.message();
        this.result=data;
    }
    public static ResponseResult SUCCESS(){
        return new ResponseResult(CoreCode.SUCCESS);
    }
    public static <T> ResponseResult<T> SUCCESS(T data){
        return new ResponseResult(CoreCode.SUCCESS, data);
    }
    public static ResponseResult FAIL(){
        return new ResponseResult(CoreCode.FAIL);
    }
    public static <T> ResponseResult<T> FAIL(T data){
        return new ResponseResult(CoreCode.FAIL, data);
    }
    public static <T> ResponseResult<T> SERVER_ERROR(T data){
        return new ResponseResult(CoreCode.SERVER_ERROR, data);
    }
}
