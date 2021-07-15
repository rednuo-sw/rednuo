package com.rednuo.core.starter;

import com.rednuo.core.response.MyResponseBodyAdvice;
import com.rednuo.core.utils.S;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * @author nz.zou 2021/7/14
 * @since avery 1.0.0
 */
@Slf4j
@Component
@Order(900)
public class CorePluginInitializer implements ApplicationRunner {

    @Autowired
    private CoreProperties coreProperties;
    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 动态修改统一响应ResponseResult 过滤的包名
        modifyResponseResultPackage();
    }

    private void modifyResponseResultPackage() throws Exception {
        // 动态修改统一响应的 包名值
        RestControllerAdvice annotation = MyResponseBodyAdvice.class.getAnnotation(RestControllerAdvice.class);
        InvocationHandler h = Proxy.getInvocationHandler(annotation);
        Field value = h.getClass().getDeclaredField("memberValues");
        value.setAccessible(true);
        Map memberValues = (Map)value.get(h);
        memberValues.put("value",coreProperties.getUrsPackage());

        System.out.println("初始化Core 统一响应ResponseResult 包名:" + S.join(coreProperties.getUrsPackage()));
    }
}
