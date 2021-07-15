package com.rednuo.avery.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.pagehelper.util.StringUtil;
import com.rednuo.avery.dto.DataListToSaveRequestExt;
import com.rednuo.avery.dto.DataToSaveRequest;
import com.rednuo.avery.entity.AveryLabelDetail;
import com.rednuo.avery.entity.HeartBeat;
import com.rednuo.avery.exception.AveryCode;
import com.rednuo.avery.mapper.AveryLabelDetailMapper;
import com.rednuo.avery.utils.ExcelUtil;
import com.rednuo.avery.utils.XmlUtils;
import com.rednuo.avery.vo.GetMachineStateExt;
import com.rednuo.avery.mapper.HeartBeatMapper;
import com.rednuo.avery.service.IAveryService;
import com.rednuo.core.exception.CustomException;
import com.rednuo.core.exception.ExceptionCast;
import com.rednuo.core.service.impl.BaseServiceImpl;
import com.rednuo.core.utils.D;
import com.rednuo.core.utils.V;
import org.dom4j.DocumentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author nz.zou 2021/7/12
 * @since avery 1.0.0
 */
@Service
@Primary
public class AveryServiceImpl extends BaseServiceImpl<AveryLabelDetailMapper, AveryLabelDetail> implements IAveryService {
    public static final Logger LOGGER = LoggerFactory.getLogger(AveryServiceImpl.class);

    private static String PATH;
    private static Integer INTERVAL_DAYS;

    static {
        try {
            PATH = XmlUtils.readXmlSingleValue(System.getProperty("user.dir") + "/config.xml", "id", "avery", "save_path");
            String str = XmlUtils.readXmlSingleValue(System.getProperty("user.dir") + "/config.xml", "id", "avery", "interval_day");
            if (str != null) {
                INTERVAL_DAYS = Integer.parseInt(str);
            } else {
                INTERVAL_DAYS = 0;
            }
        } catch (DocumentException e) {
            PATH = System.getProperty("user.dir");
            INTERVAL_DAYS = 0;
        }
    }

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    HeartBeatMapper heartBeatMapper;
    @Autowired
    AveryLabelDetailMapper averyLabelDetailMapper;

    @Value("${nz.redis-key}")
    private String nzRedisWaitToMysqlKey;

    @Value("${nz.redis-num}")
    private Integer nzRedisWaitToMysqlNum;

    @Override
    public Integer addToRedis(DataToSaveRequest dataToSaveRequest) {
        if(StringUtil.isEmpty(dataToSaveRequest.getMachine())){
            ExceptionCast.cast(AveryCode.AVERY_ADD_EMPTY_ERROR);
        }
        AveryLabelDetail averyLabelDetail = new AveryLabelDetail();

        averyLabelDetail.setBarCode(dataToSaveRequest.getBarcode());
        averyLabelDetail.setTid(dataToSaveRequest.getTid());
        averyLabelDetail.setChecipType(dataToSaveRequest.getShowChecipType());
        averyLabelDetail.setMachine(dataToSaveRequest.getMachine());
        averyLabelDetail.setOriginalEpc(dataToSaveRequest.getEpc());
        averyLabelDetail.setResult(dataToSaveRequest.getFinalResult());
        averyLabelDetail.setMachineTime(dataToSaveRequest.getScandTime());

        averyLabelDetail.setIntTime(System.currentTimeMillis());
        averyLabelDetail.setYearMoth(D.convert2FormatString(dataToSaveRequest.getScandTime(), D.FORMAT_DATE_y4Md));
        try{
            stringRedisTemplate.opsForList().rightPush(nzRedisWaitToMysqlKey, JSON.toJSONString(averyLabelDetail));
        } catch (Exception e){
            LOGGER.error("写入redis失败:e=>{} data=>{}",e.getMessage(), JSON.toJSONString(averyLabelDetail));
        }
        return 1;
    }

    @Override
    @Transactional(rollbackFor = CustomException.class)
    public Integer addToMysql(DataToSaveRequest dataToSaveRequest) {
        if(StringUtil.isEmpty(dataToSaveRequest.getMachine())){
            ExceptionCast.cast(AveryCode.AVERY_ADD_EMPTY_ERROR);
        }
        AveryLabelDetail averyLabelDetail = new AveryLabelDetail();

        averyLabelDetail.setBarCode(dataToSaveRequest.getBarcode());
        averyLabelDetail.setTid(dataToSaveRequest.getTid());
        averyLabelDetail.setChecipType(dataToSaveRequest.getShowChecipType());
        averyLabelDetail.setMachine(dataToSaveRequest.getMachine());
        averyLabelDetail.setOriginalEpc(dataToSaveRequest.getEpc());
        averyLabelDetail.setResult(dataToSaveRequest.getFinalResult());
        averyLabelDetail.setMachineTime(dataToSaveRequest.getScandTime());

        averyLabelDetail.setIntTime(System.currentTimeMillis());
        averyLabelDetail.setYearMoth(D.convert2FormatString(dataToSaveRequest.getScandTime(), D.FORMAT_DATE_y4Md));
        try{
            averyLabelDetailMapper.insert(averyLabelDetail);
        } catch (Exception e){
            LOGGER.info("写入mysql  data=>{}", JSON.toJSONString(averyLabelDetail));
        }
        return 1;
    }

    @Override
    @Transactional(rollbackFor = CustomException.class)
    public Integer listToMysql(DataListToSaveRequestExt dataListToSaveRequestExt) {
        if(dataListToSaveRequestExt.getList().size()<1){
            ExceptionCast.cast(AveryCode.AVERY_ADD_EMPTY_ERROR);
        }
        int iCount=0;
        AveryLabelDetail averyLabelDetail = null;
        for (DataToSaveRequest item: dataListToSaveRequestExt.getList()
        ) {
            averyLabelDetail = new AveryLabelDetail();

            averyLabelDetail.setBarCode(item.getBarcode());
            averyLabelDetail.setTid(item.getTid());
            averyLabelDetail.setChecipType(item.getShowChecipType());
            averyLabelDetail.setMachine(item.getMachine());
            averyLabelDetail.setOriginalEpc(item.getEpc());
            averyLabelDetail.setResult(item.getFinalResult());
            averyLabelDetail.setMachineTime(item.getScandTime());

            averyLabelDetail.setIntTime(System.currentTimeMillis());
            averyLabelDetail.setYearMoth(D.convert2FormatString(item.getScandTime(), D.FORMAT_DATE_y4Md));
            try{
                stringRedisTemplate.opsForList().rightPush(nzRedisWaitToMysqlKey, JSON.toJSONString(averyLabelDetail));
                iCount++;
            } catch (Exception e){
                LOGGER.info("写入redis  data=>{}{}", JSON.toJSONString(averyLabelDetail),e.getMessage());
            }
        }
        return iCount;
    }

    @Override
    @Transactional(rollbackFor = CustomException.class)
    public Integer heartBeat(String machineName) {
        // 记录时间
        QueryWrapper<HeartBeat> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("machine", machineName);
        List<HeartBeat> heartBeats = heartBeatMapper.selectList(queryWrapper);
        if(heartBeats.size() == 0) {
            HeartBeat heartBeat = new HeartBeat();
            heartBeat.setMachine(machineName);
            heartBeat.setIntTime(System.currentTimeMillis()/1000);
            return heartBeatMapper.insert(heartBeat);
        }
        heartBeats.get(0).setIntTime(System.currentTimeMillis()/1000);
        return heartBeatMapper.updateById(heartBeats.get(0));
    }

    @Override
    public List<GetMachineStateExt> getMachineState() {
        return heartBeatMapper.getMachineState();
    }

    @Override
    @Transactional(rollbackFor = CustomException.class)
    public boolean handleSaveToCsv(String machineName, String dateCount) {
        String dateString=null;
        int d = Integer.parseInt(dateCount);
        if(d>0) {
            dateString = D.convert2FormatString(D.addDays(new Date(), -1*(d+1)), "yyyyMMdd");
        } else{
            ExceptionCast.cast(AveryCode.AVERY_INPUT_ERROR);
            LOGGER.error("输入天数错误");
        }
        saveToCsv(machineName,dateString);
        return true;
    }

    @Override
    @Transactional(rollbackFor = CustomException.class)
    public boolean handleSaveToCsv(String machineName, Date date) {
        String dateString=null;
        if(date instanceof Date) {
            dateString = D.convert2FormatString(date, D.FORMAT_DATE_y4Md);
        } else{
            ExceptionCast.cast(AveryCode.AVERY_INPUT_ERROR);
            LOGGER.error("日期格式出错");
        }
        saveToCsv(machineName,dateString);
        return true;
    }

    @Scheduled(cron = "*/2 * * * * ?")
    @Transactional(rollbackFor = CustomException.class)
    public void scheduledSaveToMysql(){
        List<String> strAveryLabelDetailList=stringRedisTemplate.opsForList().range(nzRedisWaitToMysqlKey,0,nzRedisWaitToMysqlNum);
        if(CollectionUtils.isEmpty(strAveryLabelDetailList)){
            return;
        }
        List<AveryLabelDetail> averyLabelDetailList = new ArrayList<>();
        strAveryLabelDetailList.forEach(item -> {
            AveryLabelDetail averyLabelDetail = JSON.parseObject(item, AveryLabelDetail.class);
            averyLabelDetailList.add(averyLabelDetail);
        });
        if(V.isEmpty(averyLabelDetailList)){
            return;
        }
        long l = System.currentTimeMillis();
        boolean bl = createEntities(averyLabelDetailList);
        long x = System.currentTimeMillis()-l;
        System.out.println("redis to mysql use time:"+String.valueOf(x));
        LOGGER.info("redis write to mysql result:{}",bl);

        if (bl){
            stringRedisTemplate.opsForList().trim(nzRedisWaitToMysqlKey,strAveryLabelDetailList.size(),-1L);
        }
    }
    /**
     * 将mysql中的数据存到本地,将mysql中的原有数据清除.
     */
    @Scheduled(cron = "0 15 10 * * ?")
    @Transactional(rollbackFor = CustomException.class)
    public void scheduledSaveToCsv(){
        if(INTERVAL_DAYS == null){
            INTERVAL_DAYS = 0;
        }
        String dateString = D.convert2FormatString(D.addDays(new Date(), -1*(INTERVAL_DAYS+1)), D.FORMAT_DATE_y4Md);
        saveToCsv(null,dateString);
    }
    /**
     * 数据保存到本地
     * @param machineName m
     * @param dateString date
     */
    @Async
    public void saveToCsv(String machineName, String dateString){
        if (machineName == null){
            List<GetMachineStateExt> machineStateExts = heartBeatMapper.getMachineState();
            for(GetMachineStateExt item:machineStateExts){
                // all machine data to  save
                startSave(item.getMachine(), dateString);
            }
        }else{
            // save data for machine name
            startSave(machineName, dateString);
        }
    }

    @Transactional(rollbackFor = CustomException.class)
    public boolean startSave(String machineName, String dateString){
        if(PATH==null){
            PATH = System.getProperty("user.dir");
        }
        try {
            long l = System.currentTimeMillis();
            // 查询
            QueryWrapper<AveryLabelDetail> queryWrapper = new QueryWrapper();
            queryWrapper
                    .eq("machine", machineName)
                    .eq("year_moth", dateString);
            List<AveryLabelDetail> averyLabelDetails = averyLabelDetailMapper.selectList(queryWrapper);
            if (averyLabelDetails.size() > 0) {
                // 删除数据库中的内容
                averyLabelDetailMapper.delete(queryWrapper);
                String savePath = PATH + "/" + dateString + "/" + machineName + ".xlsx";
                //存储内容
                ExcelUtil.writeToLocal(savePath,averyLabelDetails,AveryLabelDetail.class);
            }
            long x = System.currentTimeMillis()-l;
            System.out.println("save to csv use time:"+String.valueOf(x));
            LOGGER.info("save to csv use time:"+dateString+machineName);
        }catch (Exception e){
            LOGGER.error("csv/" + dateString + machineName+" -- {}", e.getMessage());
            ExceptionCast.cast(AveryCode.AVERY_SAVE_TO_CSV_ERROR);
        }
        return true;
    }
}
