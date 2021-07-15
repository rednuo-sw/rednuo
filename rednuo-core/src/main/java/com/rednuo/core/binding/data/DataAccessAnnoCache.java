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
package com.rednuo.core.binding.data;

import com.rednuo.core.binding.QueryBuilder;
import com.rednuo.core.exception.CoreCode;
import com.rednuo.core.exception.ExceptionCast;
import com.rednuo.core.utils.BeanUtils;
import com.rednuo.core.utils.V;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 数据访问权限的注解缓存
 * @author Mazc@dibo.ltd
 * @version v2.1
 *   2020/04/24
 */
@Slf4j
public class DataAccessAnnoCache {
    /**
     * 注解缓存
     */
    private static Map<String, String[]> DATA_PERMISSION_ANNO_CACHE = new ConcurrentHashMap<>();

    /**
     * 是否有检查点注解
     * @param entityDto
     * @return 结果
     */
    public static boolean hasDataAccessCheckpoint(Class<?> entityDto){
        initClassCheckpoint(entityDto);
        String[] columns = DATA_PERMISSION_ANNO_CACHE.get(entityDto.getName());
        if(V.isEmpty(columns)){
            return false;
        }
        for(String type : columns){
            if(V.notEmpty(type)){
                return true;
            }
        }
        return false;
    }

    /**
     * 获取数据权限的用户类型列名
     * @param entityDto
     * @return 结果
     */
    public static String getDataPermissionColumn(Class<?> entityDto, CheckpointType type){
        initClassCheckpoint(entityDto);
        int typeIndex = type.index();
        String key = entityDto.getName();
        String[] columns = DATA_PERMISSION_ANNO_CACHE.get(key);
        if(columns != null && (columns.length-1) >= typeIndex){
            return columns[typeIndex];
        }
        return null;
    }

    /**
     * 初始化entityDto的检查点缓存
     * @param entityDto
     */
    private static void initClassCheckpoint(Class<?> entityDto){
        String key = entityDto.getName();
        if(!DATA_PERMISSION_ANNO_CACHE.containsKey(key)){
            String[] results = {"", "", "", "", ""};
            List<Field> fieldList = BeanUtils.extractFields(entityDto, DataAccessCheckpoint.class);
            if(V.notEmpty(fieldList)){
                for(Field fld : fieldList){
                    DataAccessCheckpoint checkpoint = fld.getAnnotation(DataAccessCheckpoint.class);
                    if(V.notEmpty(results[checkpoint.type().index()])){
                        ExceptionCast.cast(CoreCode.FAIL, entityDto.getSimpleName() + "中DataPermissionCheckpoint同类型注解重复！");
                    }
                    results[checkpoint.type().index()] = QueryBuilder.getColumnName(fld);
                }
            }
            DATA_PERMISSION_ANNO_CACHE.put(key, results);
        }
    }

}
