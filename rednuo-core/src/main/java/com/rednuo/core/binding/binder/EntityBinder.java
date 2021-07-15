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
import com.rednuo.core.exception.CoreCode;
import com.rednuo.core.exception.ExceptionCast;
import com.rednuo.core.utils.BeanUtils;
import com.rednuo.core.utils.ISetter;
import com.rednuo.core.utils.S;
import com.rednuo.core.utils.V;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Entity实体绑定Binder，用于绑定当前一个entity到目标对象的属性
 * @author mazc@dibo.ltd
 * @version v2.0
 *   2019/1/19
 */
public class EntityBinder<T> extends BaseBinder<T> {
    private static final Logger log = LoggerFactory.getLogger(EntityBinder.class);

    /***
     * 给待绑定list中VO对象赋值的setter属性名
     */
    protected String annoObjectField;
    /***
     * 给待绑定list中VO对象赋值的setter属性class类型
     */
    protected Class<?> annoObjectFieldClass;

    /***
     * 构造方法
     * @param referencedService
     * @param voList
     */
    public EntityBinder(IService<T> referencedService, List voList){
        super(referencedService, voList);
    }

    /***
     * 指定VO绑定属性赋值的setter方法
     * @param voSetter VO中调用赋值的setter方法
     * @param <T1> VO类型
     * @param <R> set方法参数类型
     * @return 结果
     */
    public <T1,R> BaseBinder<T> set(ISetter<T1, R> voSetter, Class annoObjectFieldClass){
        return set(BeanUtils.convertToFieldName(voSetter), annoObjectFieldClass);
    }

    /***
     * 指定VO绑定属性赋值的set属性
     * @param annoObjectField VO中调用赋值的setter属性
     * @return 结果
     */
    public BaseBinder<T> set(String annoObjectField, Class annoObjectFieldClass){
        this.annoObjectField = annoObjectField;
        this.annoObjectFieldClass = annoObjectFieldClass;
        return this;
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
        // 直接关联Entity
        if(middleTable == null){
            // @BindEntity(entity = Department.class, condition="this.department_id=id AND this.type=type")
            // Department department;
            super.buildQueryWrapperJoinOn();
            // 查询entity列表
            List<T> list = getEntityList(queryWrapper);
            if(V.notEmpty(list)){
                Map<String, Object> valueEntityMap = this.buildMatchKey2EntityMap(list);
                ResultAssembler.bindPropValue(annoObjectField, annoObjectList, annoObjJoinFlds, valueEntityMap);
            }
        }
        // 通过中间表关联Entity
        else{
            if(refObjJoinFlds.size() > 1){
                ExceptionCast.cast(CoreCode.FAIL, NOT_SUPPORT_MSG);
            }
            // 提取注解条件中指定的对应的列表
            Map<String, List> trunkObjCol2ValuesMap = super.buildTrunkObjCol2ValuesMap();
            // 结果转换Map
            Map<String, Object> valueEntityMap = new HashMap<>();
            Map<String, Object> middleTableResultMap = middleTable.executeOneToOneQuery(trunkObjCol2ValuesMap);
            if(V.notEmpty(middleTableResultMap)){
                String refObjJoinOnField = refObjJoinFlds.get(0);
                // 提取entity主键值集合
                Collection refObjValues = middleTableResultMap.values().stream().distinct().collect(Collectors.toList());
                // 构建查询条件
                queryWrapper.in(S.toSnakeCase(refObjJoinOnField), refObjValues);
                // 查询entity列表
                List<T> list = getEntityList(queryWrapper);
                if(V.notEmpty(list)){
                    // 转换entity列表为Map<ID, Entity>
                    Map<String, T> listMap = BeanUtils.convertToStringKeyObjectMap(list, refObjJoinOnField);
                    if(V.notEmpty(listMap)){
                        //List<String> joinOnValues = new ArrayList<>(refObjJoinFlds.size());
                        for(Map.Entry<String, Object> entry : middleTableResultMap.entrySet()){
                            Object fetchValueId = entry.getValue();
                            if(fetchValueId == null){
                                continue;
                            }
                            String key = entry.getKey();
                            T entity = listMap.get(String.valueOf(fetchValueId));
                            valueEntityMap.put(key, cloneOrConvertBean(entity));
                        }
                    }
                }
            }
            // 绑定结果
            ResultAssembler.bindPropValue(annoObjectField, annoObjectList, middleTable.getTrunkObjColMapping(), valueEntityMap);
        }
    }

    /**
     * 构建匹配key-entity目标的map
     * @param list
     * @return 结果
     */
    private Map<String, Object> buildMatchKey2EntityMap(List<T> list){
        Map<String, Object> key2TargetMap = new HashMap<>(list.size());
        List<String> joinOnValues = new ArrayList<>(refObjJoinFlds.size());
        for(T entity : list){
            joinOnValues.clear();
            for(String refObjJoinOnCol : refObjJoinFlds){
                String pkValue = BeanUtils.getStringProperty(entity, refObjJoinOnCol);
                joinOnValues.add(pkValue);
            }
            String matchKey = S.join(joinOnValues);

            Object target = entity;
            if(target instanceof Map == false){
                target = cloneOrConvertBean(entity);
            }
            key2TargetMap.put(matchKey, target);
        }
        return key2TargetMap;
    }

    /**
     * 克隆Entity/VO对象（如果与Entity类型不一致，如VO则先转型）
     * @param value
     */
    protected Object cloneOrConvertBean(T value){
        if(value == null){
            return value;
        }
        if(value.getClass().getName().equals(annoObjectFieldClass.getName())){
            return BeanUtils.cloneBean(value);
        }
        else{
            return BeanUtils.convert(value, annoObjectFieldClass);
        }
    }
}
