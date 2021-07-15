package com.rednuo.avery.vo;

import lombok.Data;
import lombok.ToString;

/**
 * @author nz.zou 2021/7/12
 * @since avery 1.0.0
 */
@Data
@ToString
public class GetMachineStateExt {
    private String machine;
    private Integer state;
}
