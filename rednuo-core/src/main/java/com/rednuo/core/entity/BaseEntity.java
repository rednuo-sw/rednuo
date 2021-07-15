package com.rednuo.core.entity;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rednuo.core.config.Cons;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * @author rednuo 2021/5/6
 */
@Getter
@Setter
@Accessors(chain = true)
public abstract class BaseEntity implements Serializable {
    public static final long serialVersionUID = 10000L;
    /**
     * 默认主键 uuid
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 默认逻辑删除标记, is_deleted = 0 有效
     */
    @TableLogic
    @JsonIgnore
    @TableField(value = Cons.COLUMN_IS_DELETED, select = false)
    private boolean deleted = false;

    /**
     * 默认记录创建时间字段,新建时由数据库赋值
     */
    @TableField(insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private Date createTime;

    /**
     * Entity对象转为Map
     * @return 结果 map
     */
    public Map<String, Object> toMap(){
        String jsonStr = JSON.toJSONString(this);
        return JSON.parseObject(jsonStr, Map.class);
    }

    /**
     * 获取主键值
     * @return 结果 obj
     */
    @JsonIgnore
    public Object getPrimaryKeyVal(){
        return null;
    }

    /**
     * Entity 对象转 String
     * @return 结果 str
     */
    @Override
    public String toString() {
        return this.getClass().getName()+":"+this.getId();
    }
}
