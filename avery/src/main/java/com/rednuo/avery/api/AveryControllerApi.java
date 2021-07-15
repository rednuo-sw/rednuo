package com.rednuo.avery.api;

import com.rednuo.avery.dto.DataToSaveRequest;
import com.rednuo.avery.vo.GetMachineStateExt;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import java.util.Date;
import java.util.List;

/**
 * @author redNuo----2020/12/16
 */
@Api(value = "Avery Api Center")
public interface AveryControllerApi {

    @ApiOperation(value = "添加数据到redis缓存处理")
    public Integer addToRedis(
            @ApiParam(name = "jsonString", type = "string", value = "{\"machine\":\"1S01\",\"tid\":\"jfkldsajfkldsjakl787\",\"epc\":\"fdajli7f8dsaf8\",\"barcode\":\"fFDSIU78yu\",\"showChecipType\":\"JIJJDI764JJJ\",\"finalResult\":\"Sucess\",\"scandTime\":\"2020-11-11 11:11:12\"}")
                    String jsonString);

    @ApiOperation(value = "添加数据直接到Mysql")
    public Integer addToMysql(DataToSaveRequest dataToSaveRequest);

    @ApiOperation(value = "批量导入MySQL")
    public Integer listToMysql(String jsonList);

    @ApiOperation(value = "设备在线,心跳")
    public Integer heartBeat(String machineName);

    @ApiOperation(value = "获得设备状态")
    public List<GetMachineStateExt> getMachineState();

    @ApiOperation(value = "手动保存数据库中的数据到CSV文件中")
    public boolean handleSaveToCsv(String machineName, String dateCount);

    @ApiOperation(value = "手动保存数据库中的数据到CSV文件中 时间格式: 2021/05/01")
    public boolean handleSaveToCsvForDate(String machineName, Date date);
}
