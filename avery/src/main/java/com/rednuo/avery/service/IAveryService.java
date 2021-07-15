package com.rednuo.avery.service;


import com.rednuo.avery.dto.DataListToSaveRequestExt;
import com.rednuo.avery.dto.DataToSaveRequest;
import com.rednuo.avery.entity.AveryLabelDetail;
import com.rednuo.avery.vo.GetMachineStateExt;
import com.rednuo.core.service.BaseService;

import java.util.Date;
import java.util.List;

/**
 * @author  redNuo----2020/12/16
 */
public interface IAveryService extends BaseService<AveryLabelDetail> {

    /**
     * 数据存到redis,分批次存到数据库
     * @param dataToSaveRequest list
     * @return 结果 结果 int
     */
    public Integer addToRedis(DataToSaveRequest dataToSaveRequest);
    /**
     * 数据存到数据库
     * @param dataToSaveRequest list
     * @return 结果 结果 int
     */
    public Integer addToMysql(DataToSaveRequest dataToSaveRequest);
    /**
     * 批量导入到数据库
     * @param dataListToSaveRequestExt list
     * @return 结果 结果 int
     */
    public Integer listToMysql(DataListToSaveRequestExt dataListToSaveRequestExt);
    /**
     * 心跳
     */
    public Integer heartBeat(String machineName);
    /**
     * 获得设备状态
     */
    public List<GetMachineStateExt> getMachineState();
    /**
     * 手动导出csv
     */
    public boolean handleSaveToCsv(String machineName, String dateCount);
    /**
     * 手动导出csv
     */
    public boolean handleSaveToCsv(String machineName, Date date);
}
