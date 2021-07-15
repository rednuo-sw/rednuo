package com.rednuo.core.config;

/**
 * 基础常量定义
 * @author rednuo 2021/4/28
 */
public class Cons {
    /**
     * 所有成功的响应Code
     */
    public static final Integer RESPONSE_SUCCESS_CODE = 10000;
    /**
     * 默认字符集UTF-8
     */
    public static final String CHARSET_UTF8 = "UTF-8";
    /**
     * 逗号分隔符 ,
     */
    public static final String SEPARATOR_COMMA = ",";
    /**
     * 下划线分隔符_
     */
    public static final String SEPARATOR_UNDERSCORE = "_";
    /**
     * 排序 - 降序标记
     */
    public static final String ORDER_DESC = "DESC";
    /**
     * 逻辑删除列名
     */
    public static final String COLUMN_IS_DELETED = "is_deleted";
    /**
     * JWT token前缀
     */
    public static final String JWT_TOKEN_PREFIX = "Bearer ";
    /**
     * JWT token header头名称
     */
    public static final String JWT_TOKEN_HEADER_NAME = "Authorization";

    /***
     * 默认字段名定义
     */
    public enum FieldName{
        /**
         * 主键属性名
         */
        id,
        /**
         * 默认的上级ID属性名
         */
        parentId,
        /**
         * 子节点属性名
         */
        children,
        /**
         * 逻辑删除标记字段
         */
        deleted,
        /**
         * 创建时间字段
         */
        createTime,
        /**
         * 更新时间
         */
        updateTime,
        /**
         * 创建人
         */
        createBy
    }
}
