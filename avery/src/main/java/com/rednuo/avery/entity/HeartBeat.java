package com.rednuo.avery.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;

/**
 * @author  nz.zou 2021/7/12
 * @since avery 1.0.0
 */
@Data
@ToString
@TableName("heart_beat")
public class HeartBeat {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("machine")
    private String machine;

    @TableField("int_time")
    private Long intTime;
}
