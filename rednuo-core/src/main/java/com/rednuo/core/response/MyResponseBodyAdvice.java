package com.rednuo.core.response;

import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 统一响应结果
 * @author nz.zou 2021/6/15
 * @since root 1.0.0
 */
@RestControllerAdvice(value = {"com.rednuo"})
public class MyResponseBodyAdvice implements ResponseBodyAdvice {
    public MyResponseBodyAdvice() {
        System.out.println("");
    }

    @Override
    public boolean supports(MethodParameter methodParameter, Class aClass) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object o, MethodParameter methodParameter, MediaType mediaType, Class aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        if (o instanceof ResponseResult){
            return o;
        }
        //responseObject是否是文件
        if (o instanceof Resource) {
            return o;
        }
        //该方法返回的媒体类型是否是application/json。若不是，直接返回响应内容
        if (!mediaType.includes(MediaType.APPLICATION_JSON)) {
            return o;
        }
        return ResponseResult.SUCCESS(o);
    }
}
