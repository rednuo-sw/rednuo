package com.rednuo.avery.dto;

import lombok.Data;
import lombok.ToString;

import java.util.List;

/**
 * @author nz.zou 2021/7/12
 * @since avery 1.0.0
 */
@Data
@ToString
public class DataListToSaveRequestExt {
    List<DataToSaveRequest> list;
}
