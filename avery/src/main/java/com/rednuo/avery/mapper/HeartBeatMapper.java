package com.rednuo.avery.mapper;

import com.rednuo.avery.vo.GetMachineStateExt;
import com.rednuo.avery.entity.HeartBeat;
import com.rednuo.core.mapper.BaseCrudMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author  nz.zou 2021/7/12
 * @since avery 1.0.0
 */
@Mapper
public interface HeartBeatMapper extends BaseCrudMapper<HeartBeat> {
    public List<GetMachineStateExt> getMachineState();
}
