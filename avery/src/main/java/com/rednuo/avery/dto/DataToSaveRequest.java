package com.rednuo.avery.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @author nz.zou 2021/7/12
 * @since avery 1.0.0
 */
@Data
@ToString
public class DataToSaveRequest {
    private String machine;
    private String tid;
    private String epc;
    private String barcode;
    private String showChecipType;
    private String finalResult;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(shape=JsonFormat.Shape.STRING,pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date scandTime;
}
