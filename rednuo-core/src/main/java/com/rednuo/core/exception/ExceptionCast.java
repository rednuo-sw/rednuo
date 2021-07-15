package com.rednuo.core.exception;

import com.rednuo.core.response.ResultCode;

/**
 * 统一抛出异常
 * @author  rednuo 2021/4/28
 */
public class ExceptionCast {
    public static void cast(ResultCode resultCode){
        //抛出异常
        throw new CustomException(resultCode);
    }
    public static void cast(ResultCode resultCode,String msg){
        //抛出异常
        throw new CustomException(resultCode,msg);
    }
}
