package com.rednuo.avery.controller;

import com.alibaba.fastjson.JSON;
import com.rednuo.avery.api.AveryControllerApi;
import com.rednuo.avery.dto.DataListToSaveRequestExt;
import com.rednuo.avery.dto.DataToSaveRequest;
import com.rednuo.avery.vo.GetMachineStateExt;
import com.rednuo.avery.service.IAveryService;
import com.rednuo.core.controller.BaseController;
import com.rednuo.core.response.MyResponseBodyAdvice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author  redNuo----2020/12/16
 */
@RestController
@RequestMapping("/avery")
public class AveryController extends BaseController implements AveryControllerApi {
    @Autowired
    IAveryService averyService;

    @Override
    @PostMapping("add_to_redis")
    public Integer addToRedis(String jsonString) {
        DataToSaveRequest dataToSaveRequest = JSON.parseObject(jsonString,DataToSaveRequest.class);
        return averyService.addToRedis(dataToSaveRequest);
    }

    @Override
    @PostMapping("add_to_mysql")
    public Integer addToMysql(DataToSaveRequest dataToSaveRequest) {
        averyService.addToMysql(dataToSaveRequest);
        return averyService.addToMysql(dataToSaveRequest);
    }

    @Override
    @PostMapping("list_to_mysql")
    public Integer listToMysql(String jsonList) {
        List<DataToSaveRequest> dataToSaveRequestList = JSON.parseArray(jsonList,DataToSaveRequest.class);
        DataListToSaveRequestExt dataListToSaveRequestExt = new DataListToSaveRequestExt();
        dataListToSaveRequestExt.setList(dataToSaveRequestList);
        return averyService.listToMysql(dataListToSaveRequestExt);
    }

    @Override
    @PostMapping("heart_beat")
    public Integer heartBeat(@RequestParam String name) {
        return averyService.heartBeat(name);
    }

    @Override
    @GetMapping("get_machine_state")
    public List<GetMachineStateExt>  getMachineState() {
        return averyService.getMachineState();
    }

    @Override
    @GetMapping("save_to_csv")
    public boolean handleSaveToCsv(String machineName, String dateCount) {
        return averyService.handleSaveToCsv(machineName,dateCount);
    }

    @Override
    @GetMapping("save_to_csv_date")
    public boolean handleSaveToCsvForDate(String machineName, Date date) {
        return averyService.handleSaveToCsv(machineName,date);
    }

    @GetMapping("testZhu")
    public boolean testZhu(){
        // 获得类注解
        RestControllerAdvice annotation = MyResponseBodyAdvice.class.getAnnotation(RestControllerAdvice.class);
        //方法注解
        List<Field> list = Arrays.asList(MyResponseBodyAdvice.class.getDeclaredFields());
        for(int i=0;i<list.size();i++) {
            Field field = list.get(i);
            if(field.isAnnotationPresent(RestControllerAdvice.class)){
                System.out.println();
            }
        }
        return true;
    }
}
