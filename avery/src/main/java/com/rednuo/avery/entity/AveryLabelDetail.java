package com.rednuo.avery.entity;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.ToString;

import java.util.Date;

/**
 * @author  nz.zou 2021/7/12
 * @since avery 1.0.0
 */
@Data
@ToString
@TableName("avery_label_detail")
public class AveryLabelDetail {

    @TableId(type = IdType.AUTO)
    @ExcelProperty(value = "ID", index = 0)
    private Long id;
    @TableField("machine")
    @ExcelProperty(value = "machine", index = 1)
    private String machine;
    @TableField("tid")
    @ExcelProperty(value = "tid", index = 2)
    private String tid;
    @TableField("original_epc")
    @ExcelProperty(value = "originalEpc", index = 3)
    private String originalEpc;
    @TableField("bar_code")
    @ExcelProperty(value = "barCode", index = 4)
    private String barCode;
    @TableField("checip_type")
    @ExcelProperty(value = "checipType", index = 5)
    private String checipType;
    @TableField()
    @ExcelProperty(value = "result", index = 6)
    private String result;

    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    @JsonFormat(shape=JsonFormat.Shape.STRING,pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    @TableField("machine_time")
    @ExcelProperty(value = "mTime", index = 7)
    private Date machineTime;

    @TableField("year_moth")
    @ExcelIgnore
    private String yearMoth;
    @TableField("int_time")
    @ExcelIgnore
    private Long intTime;
}
