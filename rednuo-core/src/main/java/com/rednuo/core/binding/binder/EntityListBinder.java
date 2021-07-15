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
package com.rednuo.core.binding.binder;

import com.baomidou.mybatisplus.extension.service.IService;
import com.rednuo.core.binding.helper.ResultAssembler;
import com.rednuo.core.config.Cons;
import com.rednuo.core.exception.CoreCode;
import com.rednuo.core.exception.ExceptionCast;
import com.rednuo.core.utils.BeanUtils;
import com.rednuo.core.utils.S;
import com.rednuo.core.utils.V;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Entity集合绑定实现
 * @author mazc@dibo.ltd
 * @version v2.0
 *   2019/1/19
 */
public class EntityListBinder<T> extends EntityBinder<T> {
    private static final Logger log = LoggerFactory.getLogger(EntityListBinder.class);

    /**
     * EntityList 排序
     */
    private String orderBy;
    public void setOrderBy(String orderBy){
        this.orderBy = orderBy;
    }

    /***
     * 构造方法
     * @param serviceInstance
     * @param voList
     */
    public EntityListBinder(IService<T> serviceInstance, List voList){
        super(serviceInstance, voList);
    }

    @Override
    public void bind() {
        if(V.isEmpty(annoObjectList)){
            return;
        }
        if(V.isEmpty(refObjJoinFlds)){
            log.warn("调用错误：无法从condition中解析出字段关联.");
            return;
        }
        Map<String, List> valueEntityListMap = new HashMap<>();
        if(middleTable == null){
            super.buildQueryWrapperJoinOn();
            //处理orderBy，附加排序
            this.appendOrderBy();
            // 查询entity列表
            List<T> list = getEntityList(queryWrapper);
            if(V.notEmpty(list)){
                valueEntityListMap = this.buildMatchKey2EntityListMap(list);
            }
            ResultAssembler.bindPropValue(annoObjectField, annoObjectList, annoObjJoinFlds, valueEntityListMap);
        }
        else{
            if(refObjJoinFlds.size() > 1){
                ExceptionCast.cast(CoreCode.FAIL, NOT_SUPPORT_MSG);
            }
            // 提取注解条件中指定的对应的列表
            Map<String, List> trunkObjCol2ValuesMap = super.buildTrunkObjCol2ValuesMap();
            Map<String, List> middleTableResultMap = middleTable.executeOneToManyQuery(trunkObjCol2ValuesMap);
            if(V.isEmpty(middleTableResultMap)){
                return;
            }
            String refObjJoinOnField = refObjJoinFlds.get(0);
            // 收集查询结果values集合
            List entityIdList = extractIdValueFromMap(middleTableResultMap);
            // 构建查询条件
            queryWrapper.in(S.toSnakeCase(refObjJoinOnField), entityIdList);
            //处理orderBy，附加排序
            this.appendOrderBy();
            // 查询entity列表: List<Role>
            List list = getEntityList(queryWrapper);
            if(V.isEmpty(list)){
                return;
            }
            // 转换entity列表为Map<ID, Entity>
            Map<String, T> entityMap = BeanUtils.convertToStringKeyObjectMap(list, refObjJoinOnField);
            for(Map.Entry<String, List> entry : middleTableResultMap.entrySet()){
                // List<roleId>
                List annoObjFKList = entry.getValue();
                if(V.isEmpty(annoObjFKList)){
                    continue;
                }
                List valueList = new ArrayList();
                for(Object obj : annoObjFKList){
                    T ent = entityMap.get(String.valueOf(obj));
                    if(ent != null){
                        valueList.add(cloneOrConvertBean(ent));
                    }
                }
                valueEntityListMap.put(entry.getKey(), valueList);
            }
            // 绑定结果
            ResultAssembler.bindPropValue(annoObjectField, annoObjectList, middleTable.getTrunkObjColMapping(), valueEntityListMap);
        }
    }

    /**
     * 构建匹配key-entity目标的map
     * @param list
     * @return 结果
     */
    private Map<String, List> buildMatchKey2EntityListMap(List<T> list){
        Map<String, List> key2TargetListMap = new HashMap<>(list.size());
        List<String> joinOnValues = new ArrayList<>(refObjJoinFlds.size());
        for(T entity : list){
            joinOnValues.clear();
            for(String refObjJoinOnCol : refObjJoinFlds){
                String pkValue = BeanUtils.getStringProperty(entity, refObjJoinOnCol);
                joinOnValues.add(pkValue);
            }
            String matchKey = S.join(joinOnValues);
            // 获取list
            List entityList = key2TargetListMap.get(matchKey);
            if(entityList == null){
                entityList = new ArrayList<>();
                key2TargetListMap.put(matchKey, entityList);
            }
            Object target = entity;
            if(target instanceof Map == false){
                target = cloneOrConvertBean(entity);
            }
            entityList.add(target);
        }
        return key2TargetListMap;
    }

    /**
     * 附加排序字段，支持格式：orderBy=shortName:DESC,age:ASC,birthdate
     */
    private void appendOrderBy(){
        if(V.isEmpty(this.orderBy)){
            return;
        }
        // 解析排序
        String[] orderByFields = S.split(this.orderBy);
        for(String field : orderByFields){
            if(field.contains(":")){
                String[] fieldAndOrder = S.split(field, ":");
                String columnName = S.toSnakeCase(fieldAndOrder[0]);
                if(Cons.ORDER_DESC.equalsIgnoreCase(fieldAndOrder[1])){
                    queryWrapper.orderByDesc(columnName);
                }
                else{
                    queryWrapper.orderByAsc(columnName);
                }
            }
            else{
                queryWrapper.orderByAsc(S.toSnakeCase(field));
            }
        }
    }
}
