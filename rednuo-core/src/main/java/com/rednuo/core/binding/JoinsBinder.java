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
package com.rednuo.core.binding;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.rednuo.core.binding.helper.ServiceAdaptor;
import com.rednuo.core.binding.parser.ParserCache;
import com.rednuo.core.binding.query.dynamic.AnnoJoiner;
import com.rednuo.core.binding.query.dynamic.DynamicJoinQueryWrapper;
import com.rednuo.core.config.BaseConfig;
import com.rednuo.core.config.Cons;
import com.rednuo.core.exception.CoreCode;
import com.rednuo.core.exception.ExceptionCast;
import com.rednuo.core.mapper.DynamicQueryMapper;
import com.rednuo.core.utils.BeanUtils;
import com.rednuo.core.utils.ContextHelper;
import com.rednuo.core.utils.S;
import com.rednuo.core.utils.V;
import com.rednuo.core.vo.Pagination;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.*;

/**
 * join连接查询绑定器
 * @author Mazc@dibo.ltd
 * @version v2.1
 *   2020/04/15
 */
@Slf4j
public class JoinsBinder {

    /**
     * 关联查询一条数据
     * @param queryWrapper 查询条件
     * @param entityClazz 返回结果entity/vo类
     * @return 结果
     */
    public static <DTO,E> E queryOne(QueryWrapper<DTO> queryWrapper, Class<E> entityClazz){
        List<E> list = executeJoinQuery(queryWrapper, entityClazz, null, true);
        if(V.notEmpty(list)){
            return list.get(0);
        }
        return null;
    }

    /**
     * 关联查询符合条件的全部数据集合（不分页）
     * @param queryWrapper 调用QueryBuilder.to*QueryWrapper得到的实例
     * @param entityClazz 返回结果entity/vo类
     * @return 结果
     */
    public static <DTO,E> List<E> queryList(QueryWrapper<DTO> queryWrapper, Class<E> entityClazz){
        return queryList(queryWrapper, entityClazz, null);
    }

    /**
     * 关联查询符合条件的指定页数据（分页）
     * @param queryWrapper 调用QueryBuilder.to*QueryWrapper得到的实例
     * @param entityClazz 返回结果entity/vo类
     * @param pagination 分页
     * @return 结果
     */
    public static <DTO,E> List<E> queryList(QueryWrapper<DTO> queryWrapper, Class<E> entityClazz, Pagination pagination){
        return executeJoinQuery(queryWrapper, entityClazz, pagination, false);
    }

    /**
     * 关联查询（分页）
     * @param queryWrapper 调用QueryBuilder.to*QueryWrapper得到的实例
     * @param entityClazz 返回结果entity/vo类
     * @param pagination 分页
     * @return 结果
     */
    private static <DTO,E> List<E> executeJoinQuery(QueryWrapper<DTO> queryWrapper, Class<E> entityClazz, Pagination pagination, boolean limit1){
        // 非动态查询，走BaseService
        if(queryWrapper instanceof DynamicJoinQueryWrapper == false){
            IService iService = ContextHelper.getIServiceByEntity(entityClazz);
            if(iService != null){
                return ServiceAdaptor.queryList(iService, (QueryWrapper)queryWrapper, pagination, entityClazz);
            }
            else{
                ExceptionCast.cast(CoreCode.FAIL, "单表查询对象无BaseService/IService实现: "+entityClazz.getSimpleName());
            }
        }
        long begin = System.currentTimeMillis();
        // 转换为queryWrapper
        DynamicJoinQueryWrapper dynamicJoinWrapper = (DynamicJoinQueryWrapper)queryWrapper;
        dynamicJoinWrapper.setMainEntityClass(entityClazz);
        List<Map<String, Object>> mapList = null;
        if(pagination == null){
            if(limit1){
                Page page = new Page<>(1, 1);
                page.setSearchCount(false);
                IPage<Map<String, Object>> pageResult = getDynamicQueryMapper().queryForListWithPage(page, dynamicJoinWrapper);
                mapList = pageResult.getRecords();
            }
            else{
                mapList = getDynamicQueryMapper().queryForList(dynamicJoinWrapper);
            }
        }
        else{
            // 格式化orderBy
            formatOrderBy(dynamicJoinWrapper, entityClazz, pagination);
            IPage<Map<String, Object>> pageResult = getDynamicQueryMapper().queryForListWithPage(pagination.toPage(), dynamicJoinWrapper);
            pagination.setTotalCount(pageResult.getTotal());
            mapList = pageResult.getRecords();
        }
        long ms = (System.currentTimeMillis() - begin);
        if(ms > 5000){
            log.warn("{} 动态Join查询执行耗时 {} ms，建议优化", dynamicJoinWrapper.getDtoClass().getSimpleName(), ms);
        }
        if(V.isEmpty(mapList)){
            return Collections.emptyList();
        }
        if(mapList.size() > BaseConfig.getBatchSize()){
            log.warn("{} 动态Join查询记录数过大( {} 条), 建议优化", dynamicJoinWrapper.getDtoClass().getSimpleName(), mapList.size());
        }
        // 转换查询结果
        List<E> entityList = new ArrayList<>();
        for(Map<String, Object> colValueMap : mapList){
            Map<String, Object> fieldValueMap = new HashMap<>();
            // 格式化map
            for(Map.Entry<String, Object> entry : colValueMap.entrySet()){
                String fieldName = S.toLowerCaseCamel(entry.getKey());
                // 如果是布尔类型，检查entity中的定义是Boolean/boolean
                if(entry.getValue() instanceof Boolean && S.startsWithIgnoreCase(entry.getKey(),"is_")){
                    // 检查有is前缀的Boolean类型
                    Field boolType = BeanUtils.extractField(entityClazz, fieldName);
                    if(boolType == null){
                        // 检查无is前缀的boolean类型
                        String tempFieldName = S.toLowerCaseCamel(S.substringAfter(entry.getKey(), "_"));
                        boolType = BeanUtils.extractField(entityClazz, tempFieldName);
                        if(boolType != null){
                            fieldName = tempFieldName;
                        }
                    }
                }
                fieldValueMap.put(fieldName, entry.getValue());
            }
            // 绑定map到entity
            try{
                E entityInst = entityClazz.newInstance();
                BeanUtils.bindProperties(entityInst, fieldValueMap);
                entityList.add(entityInst);
            }
            catch (Exception e){
                log.warn("new实例并绑定属性值异常", e);
            }
        }
        return entityList;
    }

    /**
     * 格式化orderBy
     * @param queryWrapper 查询条件
     * @param pagination p
     */
    private static <E> void formatOrderBy(DynamicJoinQueryWrapper queryWrapper, Class<E> entityClazz, Pagination pagination){
        // 如果是默认id排序，检查是否有id字段
        if(pagination.isDefaultOrderBy()){
            // 优化排序
            String pk = ContextHelper.getPrimaryKey(entityClazz);
            // 主键非有序id字段，需要清空默认排序
            if (!Cons.FieldName.id.name().equals(pk)) {
                pagination.clearDefaultOrder();
            }
        }
        // 格式化排序
        if(V.notEmpty(pagination.getOrderBy())){
            List<String> orderByList = new ArrayList<>();
            String[] orderByFields = S.split(pagination.getOrderBy());
            for(String field : orderByFields){
                String fieldName = field, orderType = null;
                if(field.contains(":")){
                    String[] fieldAndOrder = S.split(field, ":");
                    fieldName = fieldAndOrder[0];
                    orderType = fieldAndOrder[1];
                }
                // 获取列定义的AnnoJoiner 得到别名
                AnnoJoiner joiner = ParserCache.getAnnoJoiner(queryWrapper.getDtoClass(), fieldName);
                if(joiner != null){
                    if(V.notEmpty(joiner.getAlias())){
                        fieldName = joiner.getAlias() + "." + joiner.getColumnName();
                    }
                    else{
                        fieldName = "self." + joiner.getColumnName();
                    }
                }
                else{
                    fieldName = "self." + S.toSnakeCase(fieldName);
                }
                if(V.notEmpty(orderType)){
                    orderByList.add(fieldName + ":" + orderType);
                }
                else{
                    orderByList.add(fieldName);
                }
            }
            pagination.setOrderBy(S.join(orderByList));
        }
    }

    /**
     * 获取mapper实例
     * @return 结果
     */
    private static DynamicQueryMapper getDynamicQueryMapper(){
        return ContextHelper.getBean(DynamicQueryMapper.class);
    }

}
