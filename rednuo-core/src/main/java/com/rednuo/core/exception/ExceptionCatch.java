package com.rednuo.core.exception;


import com.google.common.collect.ImmutableMap;
import com.rednuo.core.response.ResponseResult;
import com.rednuo.core.response.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 统一异常捕获类,用于捕获未定义的异常类
 * ControllerAdvice//* 控制器增强类 注解
 * @author  rednuo 2021/4/28
 */
@RestControllerAdvice
public class ExceptionCatch {
    public static final Logger LOGGER = LoggerFactory.getLogger(ExceptionCast.class);

    /**
     * 定义Map,配置异常类型所对应的代码,这个Map是线程安全的
     * 所有的异常都会继承Throwable,并且所有的错误都依赖
     */
    public static ImmutableMap<Class<? extends Throwable>, ResultCode> EXCEPTIONS;

    /**
     * 构建ImmutableMap
     * 捕获的未知异常,统一管理
     */
    protected static ImmutableMap.Builder<Class<? extends Throwable>,ResultCode> builder = ImmutableMap.builder();
    static{
        //TODO:定义了异常类型所有对应的错误代码,HttpMessageNotReadableException就是参数异常,这些异常类需要单独处理的即整理
        builder.put(HttpMessageNotReadableException.class, CoreCode.INVALID_PARAM);
        builder.put(MethodArgumentNotValidException.class, CoreCode.INVALID_PARAM);
        builder.put(IllegalArgumentException.class, CoreCode.INVALID_DATE);
    }

    /**
     *
     * ExceptionHandler捕获CustomException的所有已知的异常
     * @param customException 自定义的异常类
     * @return 结果 返回值
     */
    @ExceptionHandler(CustomException.class)
    @ResponseBody
    public ResponseResult customException(CustomException customException){
        LOGGER.error("catch customException:{}",customException.getMessage());
        ResultCode resultCode = customException.getResultCode();
        return new ResponseResult(resultCode);
    }

    /**
     * ExceptionHandler捕获Exception的所有已知的异常
     * @param exception 未知的异常类
     * @return 结果 返回值
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseResult customException(Exception exception){
        LOGGER.error("catch Exception:{}",exception.getMessage());
        //
        if(EXCEPTIONS == null)
        {
            EXCEPTIONS = builder.build();
        }
        //从EXCEPTIONS中找异常代码,如果找到了,就将错误代码相应到错误代码,否则就返回统一异常=
        ResultCode resultCode = EXCEPTIONS.get(exception.getClass());
        if(resultCode != null){
            //返回处理的拦截异常
            return ResponseResult.FAIL(resultCode);
        }
        //返回99999异常
        return ResponseResult.SERVER_ERROR(exception.getClass().getSimpleName());
    }
}
