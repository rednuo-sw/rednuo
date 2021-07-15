/*
 * Copyright (c) 2015-2020, www.dibo.ltd (service@dibo.ltd).
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.rednuo.core.binding.query.dynamic;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.rednuo.core.binding.helper.ServiceAdaptor;
import com.rednuo.core.binding.parser.ParserCache;
import com.rednuo.core.exception.CoreCode;
import com.rednuo.core.exception.ExceptionCast;
import com.rednuo.core.utils.ContextHelper;
import com.rednuo.core.vo.Pagination;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 动态查询wrapper
 * @author Mazc@dibo.ltd
 * @version v2.0 2020/04/16
 */
public class ExtQueryWrapper<DTO,E> extends QueryWrapper<DTO> {
    /**
     * 主实体class
     */
    @Getter
    @Setter
    private Class<E> mainEntityClass;

    /**
     * 获取entity表名
     */
    public String getEntityTable(){
        return ParserCache.getEntityTableName(getMainEntityClass());
    }

    /**
     * 查询一条数据
     * @param entityClazz clazz
     * @return 结果 结果 E
     */
    public E queryOne(Class<E> entityClazz){
        this.mainEntityClass = entityClazz;
        IService<E> iService = ContextHelper.getIServiceByEntity(this.mainEntityClass);
        if(iService != null){
            return ServiceAdaptor.getSingleEntity(iService, this);
        }
        else{
            ExceptionCast.cast(CoreCode.FAIL, "查询对象无BaseService/IService实现: "+this.mainEntityClass.getSimpleName());
        }
        return null;
    }

    /**
     * 查询一条数据
     * @param entityClazz clazz
     * @return 结果 结果 list
     */
    public List<E> queryList(Class<E> entityClazz){
        this.mainEntityClass = entityClazz;
        IService iService = ContextHelper.getIServiceByEntity(entityClazz);
        if(iService != null){
            return ServiceAdaptor.queryList(iService, this);
        }
        else{
            ExceptionCast.cast(CoreCode.FAIL, "查询对象无BaseService/IService实现: "+entityClazz.getSimpleName());
        }
        return null;
    }

    /**
     * 查询一条数据
     * @param entityClazz class
     * @return 结果 结果 list
     */
    public List queryList(Class<E> entityClazz, Pagination pagination){
        this.mainEntityClass = entityClazz;
        IService iService = ContextHelper.getIServiceByEntity(entityClazz);
        if(iService != null){
            return ServiceAdaptor.queryList(iService, (QueryWrapper)this, pagination, entityClazz);
        }
        else{
            ExceptionCast.cast(CoreCode.FAIL, "查询对象无BaseService/IService实现: "+entityClazz.getSimpleName());
        }
        return null;
    }

}
