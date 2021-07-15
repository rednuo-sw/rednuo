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
 * 关联字段绑定
 * @author mazc@dibo.ltd
 * @version v2.0
 *   2019/1/19
 */
public class FieldListBinder<T> extends FieldBinder<T> {
    private static final Logger log = LoggerFactory.getLogger(FieldListBinder.class);

    /***
     * 构造方法
     * @param serviceInstance s
     * @param voList v
     */
    public FieldListBinder(IService<T> serviceInstance, List voList) {
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
        if(referencedGetterColumnNameList == null){
            log.error("调用错误：字段绑定必须指定字段field");
            return;
        }
        Map<String, List> valueEntityListMap = new HashMap<>();
        List<String> selectColumns = new ArrayList<>(referencedGetterColumnNameList.size()+1);
        for(String refObjJoinOn : refObjJoinFlds){
            selectColumns.add(S.toSnakeCase(refObjJoinOn));
        }
        selectColumns.addAll(referencedGetterColumnNameList);
        queryWrapper.select(S.toStringArray(selectColumns));
        // 直接关联
        if(middleTable == null){
            super.buildQueryWrapperJoinOn();
            // 查询entity列表: List<Role>
            List<T> list = getEntityList(queryWrapper);
            if(V.notEmpty(list)){
                valueEntityListMap = this.buildMatchKey2FieldListMap(list);
            }
            // 遍历list并赋值
            bindPropValue(annoObjectList, annoObjJoinFlds, valueEntityListMap);
        }
        // 通过中间表关联
        else{
            if(refObjJoinFlds.size() > 1){
                ExceptionCast.cast(CoreCode.FAIL, NOT_SUPPORT_MSG);
            }
            // 提取注解条件中指定的对应的列表
            Map<String, List> trunkObjCol2ValuesMap = super.buildTrunkObjCol2ValuesMap();
            // 处理中间表, 将结果转换成map
            Map<String, List> middleTableResultMap = middleTable.executeOneToManyQuery(trunkObjCol2ValuesMap);
            if(V.isEmpty(middleTableResultMap)){
                return;
            }
            String refObjJoinOnField = refObjJoinFlds.get(0);
            // 收集查询结果values集合
            List entityIdList = extractIdValueFromMap(middleTableResultMap);
            // 构建查询条件
            queryWrapper.in(S.toSnakeCase(refObjJoinOnField), entityIdList);
            // 查询entity列表: List<Role>
            List<T> list = getEntityList(queryWrapper);
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
                        valueList.add(ent);
                    }
                }
                valueEntityListMap.put(entry.getKey(), valueList);
            }
            // 遍历list并赋值
            bindPropValue(annoObjectList, middleTable.getTrunkObjColMapping(), valueEntityListMap);
        }
    }

    /***
     * 从对象集合提取某个属性值到list中
     * @param fromList f
     * @param getterFields g
     * @param valueMatchMap  v
     * @param <E> e
     */
    public <E> void bindPropValue(List<E> fromList, List<String> getterFields, Map<String, List> valueMatchMap){
        if(V.isEmpty(fromList) || V.isEmpty(valueMatchMap)){
            return;
        }
        List<String> fieldValues = new ArrayList<>(getterFields.size());
        try{
            for(E object : fromList){
                fieldValues.clear();
                for(String getterField : getterFields){
                    String fieldValue = BeanUtils.getStringProperty(object, getterField);
                    fieldValues.add(fieldValue);
                }
                // 查找匹配Key
                String matchKey = S.join(fieldValues);
                List entityList = valueMatchMap.get(matchKey);
                if(entityList != null){
                    // 赋值
                    for(int i = 0; i< annoObjectSetterPropNameList.size(); i++){
                        List valObjList = BeanUtils.collectToList(entityList, S.toLowerCaseCamel(referencedGetterColumnNameList.get(i)));
                        BeanUtils.setProperty(object, annoObjectSetterPropNameList.get(i), valObjList);
                    }
                }
            }
        }
        catch (Exception e){
            log.warn("设置属性值异常", e);
        }
    }

    /***
     * 从对象集合提取某个属性值到list中
     * @param fromList f
     * @param trunkObjColMapping t
     * @param valueMatchMap v
     * @param <E> e
     */
    public <E> void bindPropValue(List<E> fromList, Map<String, String> trunkObjColMapping, Map<String, List> valueMatchMap){
        if(V.isEmpty(fromList) || V.isEmpty(valueMatchMap)){
            return;
        }
        List<String> fieldValues = new ArrayList<>(trunkObjColMapping.size());
        try{
            for(E object : fromList){
                fieldValues.clear();
                for(Map.Entry<String, String> entry :trunkObjColMapping.entrySet()){
                    String getterField = S.toLowerCaseCamel(entry.getKey());
                    String fieldValue = BeanUtils.getStringProperty(object, getterField);
                    fieldValues.add(fieldValue);
                }
                // 查找匹配Key
                String matchKey = S.join(fieldValues);
                List entityList = valueMatchMap.get(matchKey);
                if(entityList != null){
                    // 赋值
                    for(int i = 0; i< annoObjectSetterPropNameList.size(); i++){
                        List valObjList = BeanUtils.collectToList(entityList, S.toLowerCaseCamel(referencedGetterColumnNameList.get(i)));
                        BeanUtils.setProperty(object, annoObjectSetterPropNameList.get(i), valObjList);
                    }
                }
            }
        }
        catch (Exception e){
            log.warn("设置属性值异常", e);
        }
    }

    /**
     * 构建匹配key-entity目标的map
     * @param list l
     * @return 结果
     */
    private Map<String, List> buildMatchKey2FieldListMap(List<T> list){
        Map<String, List> key2TargetListMap = new HashMap<>(list.size());
        List<String> joinOnValues = new ArrayList<>(refObjJoinFlds.size());
        for(T entity : list){
            joinOnValues.clear();
            for(String refObjJoinOnCol : refObjJoinFlds){
                String fldValue = BeanUtils.getStringProperty(entity, refObjJoinOnCol);
                joinOnValues.add(fldValue);
            }
            String matchKey = S.join(joinOnValues);
            // 获取list
            List entityList = key2TargetListMap.get(matchKey);
            if(entityList == null){
                entityList = new ArrayList<>();
                key2TargetListMap.put(matchKey, entityList);
            }
            entityList.add(entity);
        }
        return key2TargetListMap;
    }

}
