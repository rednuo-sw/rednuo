package com.rednuo.core.utils;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

/**
 * 配置文件工具类
 * @author  rednuo 2021/4/28
 */
@Slf4j
public class PropertiesUtils {

    private static Environment environment;

    /**
     * 绑定Environment
     * @param env
     */
    public static void bindEnvironment(Environment env){
        environment = env;
    }

    /***
     *  读取配置项的值
     * @param key
     * @return 结果
     */
    public static String get(String key){
        if(environment == null){
            try{
                environment = ContextHelper.getApplicationContext().getEnvironment();
            }
            catch (Exception e){
                log.warn("无法获取Environment，参数配置可能不生效");
            }
        }
        // 获取配置值
        if(environment == null){
            log.warn("无法获取上下文Environment，请在Spring初始化之后调用!");
            return null;
        }
        String value = environment.getProperty(key);
        // 任何password相关的参数需解密
        boolean isSensitiveConfig = key.contains(".password") || key.contains(".secret");
        if(value != null && isSensitiveConfig){
            value = Encryptor.decrypt(value);
        }
        return value;
    }

    /***
     *  读取int型的配置项
     * @param key
     * @return 结果
     */
    public static Integer getInteger(String key){
        // 获取配置值
        String value = get(key);
        if(V.notEmpty(value)){
            return Integer.parseInt(value);
        }
        return null;
    }

    /***
     * 读取boolean值的配置项
     */
    public static boolean getBoolean(String key) {
        // 获取配置值
        String value = get(key);
        if(V.notEmpty(value)){
            return V.isTrue(value);
        }
        return false;
    }
}
