package com.rednuo.core.config;

import com.rednuo.core.utils.PropertiesUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 系统默认配置
 * @author rednuo 2021/4/28
 */
@Slf4j
public class BaseConfig {

    /**
     * 从当前配置文件获取配置参数值
     * @param key
     * @return 结果
     */
    public static String getProperty(String key){
        return PropertiesUtils.get(key);
    }

    /**
     * 从当前配置文件获取配置参数值
     * @param key
     * @param defaultValue 默认值
     * @return 结果
     */
    public static String getProperty(String key, String defaultValue){
        String value = PropertiesUtils.get(key);
        return value != null? value : defaultValue;
    }

    /***
     *  从默认的/指定的 Properties文件获取boolean值
     * @param key
     * @return 结果
     */
    public static boolean isTrue(String key){
        return PropertiesUtils.getBoolean(key);
    }

    /***
     * 获取int类型
     * @param key
     * @return 结果
     */
    public static Integer getInteger(String key){
        return PropertiesUtils.getInteger(key);
    }

    /***
     * 获取int类型
     * @param key
     * @return 结果
     */
    public static Integer getInteger(String key, int defaultValue){
        Integer value = PropertiesUtils.getInteger(key);
        return value != null? value : defaultValue;
    }

    private static Integer cutLength = null;
    /***
     * 获取截取长度
     * @return 结果
     */
    public static int getCutLength(){
        if(cutLength == null){
            cutLength = PropertiesUtils.getInteger("system.default.cutLength");
            if(cutLength == null){
                cutLength = 20;
            }
        }
        return cutLength;
    }

    private static Integer pageSize = null;
    /***
     * 默认页数
     * @return 结果
     */
    public static int getPageSize() {
        if(pageSize == null){
            pageSize = PropertiesUtils.getInteger("system.pagination.pageSize");
            if(pageSize == null){
                pageSize = 20;
            }
        }
        return pageSize;
    }

    private static Integer batchSize = null;
    /***
     * 获取批量插入的每批次数量
     * @return 结果
     */
    public static int getBatchSize() {
        if(batchSize == null){
            batchSize = PropertiesUtils.getInteger("system.batch.size");
            if(batchSize == null){
                batchSize = 1000;
            }
        }
        return batchSize;
    }

    private static String ACTIVE_FLAG_VALUE = null;
    /**
     * 获取有效记录的标记值，如 0
     * @return 结果
     */
    public static String getActiveFlagValue(){
        if(ACTIVE_FLAG_VALUE == null){
            ACTIVE_FLAG_VALUE = getProperty("mybatis-plus.global-config.db-config.logic-not-delete-value", "0");
        }
        return ACTIVE_FLAG_VALUE;
    }
}
