package com.rednuo.core.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * keyValue 键值对形式的VO
 * @author  nz.zou 2021/5/6
 * @since rednuo 1.0.0
 */
@Getter @Setter @Accessors(chain = true)
public class KeyValue implements Serializable {
    public static final long serialVersionUID = 10010L;

    public KeyValue(){}

    public KeyValue(String key, Object value){
        this.k = key;
        this.v = value;
    }

    /***
     * key: 显示值，需要显示的name/label文本
     */
    private String k;

    /***
     * value: 存储值
     */
    private Object v;

    /**
     * 扩展值
     */
    private Object ext;
}
