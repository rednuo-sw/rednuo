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
package com.rednuo.core.binding.parser;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.rednuo.core.binding.query.BindQuery;
import com.rednuo.core.binding.query.dynamic.AnnoJoiner;
import com.rednuo.core.exception.CoreCode;
import com.rednuo.core.exception.ExceptionCast;
import com.rednuo.core.utils.BeanUtils;
import com.rednuo.core.utils.ContextHelper;
import com.rednuo.core.utils.S;
import com.rednuo.core.utils.V;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 *  对象中的绑定注解 缓存管理类
 * @author mazc@dibo.ltd
 * @version 2.0
 *   2019/04/03
 */
@Slf4j
public class ParserCache {
    /**
     * VO类-绑定注解缓存
     */
    private static Map<Class, BindAnnotationGroup> allVoBindAnnotationCacheMap = new ConcurrentHashMap<>();
    /**
     * 表及相关信息的缓存
     */
    private static Map<String, TableLinkage> tableToLinkageCacheMap = new ConcurrentHashMap<>();
    /**
     * entity类-表名的缓存
     */
    private static Map<String, String> entityClassTableCacheMap = new ConcurrentHashMap<>();
    /**
     * entity类小驼峰实例名-entity类
     */
    private static Map<String, Class<?>> entityName2EntityClassCacheMap = new ConcurrentHashMap<>();
    /**
     * dto类-BindQuery注解的缓存
     */
    private static Map<String, List<AnnoJoiner>> dtoClassBindQueryCacheMap = new ConcurrentHashMap<>();

    /**
     * 获取指定class对应的Bind相关注解
     * @param voClass c
     * @return 结果
     */
    public static BindAnnotationGroup getBindAnnotationGroup(Class voClass){
        BindAnnotationGroup group = allVoBindAnnotationCacheMap.get(voClass);
        if(group == null){
            // 获取注解并缓存
            group = new BindAnnotationGroup();
            // 获取当前VO的注解
            List<Field> fields = BeanUtils.extractAllFields(voClass);
            if(fields != null){
                for (Field field : fields) {
                    //遍历属性
                    Annotation[] annotations = field.getDeclaredAnnotations();
                    if (V.isEmpty(annotations)) {
                        continue;
                    }
                    for (Annotation annotation : annotations) {
                        Class<?> setterObjClazz = field.getType();
                        if(setterObjClazz.equals(List.class) || setterObjClazz.equals(Collections.class)){
                            // 如果是集合，获取其泛型参数class
                            Type genericType = field.getGenericType();
                            if(genericType instanceof ParameterizedType){
                                ParameterizedType pt = (ParameterizedType) genericType;
                                setterObjClazz = (Class<?>)pt.getActualTypeArguments()[0];
                            }
                        }
                        group.addBindAnnotation(field.getName(), setterObjClazz, annotation);
                    }
                }
            }
            allVoBindAnnotationCacheMap.put(voClass, group);
        }
        // 返回归类后的注解对象
        return group;
    }

    /**
     * 初始化Table的相关对象信息
     */
    private static void initTableToLinkageCacheMap(){
        if(tableToLinkageCacheMap.isEmpty()){
            SqlSessionFactory sqlSessionFactory = ContextHelper.getBean(SqlSessionFactory.class);
            Collection<Class<?>> mappers = sqlSessionFactory.getConfiguration().getMapperRegistry().getMappers();
            if(V.notEmpty(mappers)){
                mappers.forEach(m->{
                    Type[] types = m.getGenericInterfaces();
                    try{
                        if(types != null && types.length > 0 && types[0] != null){
                            ParameterizedType genericType = (ParameterizedType) types[0];
                            Type[] superTypes = genericType.getActualTypeArguments();
                            if(superTypes != null && superTypes.length > 0 && superTypes[0] != null){
                                String entityClassName = superTypes[0].getTypeName();
                                if(entityClassName.length() > 1){
                                    Class<?> entityClass = Class.forName(entityClassName);
                                    TableLinkage linkage = new TableLinkage(entityClass, m);
                                    tableToLinkageCacheMap.put(linkage.getTable(), linkage);
                                    entityName2EntityClassCacheMap.put(entityClass.getSimpleName(), entityClass);
                                }
                            }
                        }
                    }
                    catch (Exception e){
                        log.warn("解析mapper异常", e);
                    }
                });
            }
        }
    }

    /**
     * 是否有is_deleted列
     * @return 结果
     */
    public static boolean hasDeletedColumn(String table){
        TableLinkage linkage = getTableLinkage(table);
        return linkage != null && linkage.isHasDeleted();
    }

    /**
     * 获取table相关信息
     * @return 结果
     */
    public static TableLinkage getTableLinkage(String table){
        initTableToLinkageCacheMap();
        TableLinkage linkage = tableToLinkageCacheMap.get(table);
        return linkage;
    }

    /**
     * 获取entity对应的表名
     * @param entityClass class
     * @return 结果
     */
    public static String getEntityTableName(Class<?> entityClass){
        String entityClassName = entityClass.getName();
        String tableName = entityClassTableCacheMap.get(entityClassName);
        if(tableName == null){
            TableName tableNameAnno = AnnotationUtils.findAnnotation(entityClass, TableName.class);
            if(tableNameAnno != null){
                tableName = tableNameAnno.value();
            }
            else{
                tableName = S.toSnakeCase(entityClass.getSimpleName());
            }
            entityClassTableCacheMap.put(entityClassName, tableName);
        }
        return tableName;
    }

    /**
     * 根据entity类获取mapper实例
     * @return 结果
     */
    public static BaseMapper getMapperInstance(Class<?> entityClass){
        String tableName = getEntityTableName(entityClass);
        TableLinkage linkage = getTableLinkage(tableName);
        if(linkage == null){
            ExceptionCast.cast(CoreCode.FAIL_INVALID_PARAM, "未找到 "+entityClass.getName()+" 的Mapper定义！");
        }
        BaseMapper mapper = (BaseMapper) ContextHelper.getBean(linkage.getMapperClass());
        return mapper;
    }

    /**
     * 根据类的entity类名获取EntityClass
     * @return 结果
     */
    public static Class<?> getEntityClassByClassName(String className){
        initTableToLinkageCacheMap();
        return entityName2EntityClassCacheMap.get(className);
    }

    /**
     * 当前DTO是否有Join绑定
     * @param dto dto对象
     * @param fieldNameSet 有值属性集合
     * @param <DTO> dto
     * @return 结果
     */
    public static <DTO> boolean hasJoinTable(DTO dto, Collection<String> fieldNameSet){
        List<AnnoJoiner> annoList = getBindQueryAnnos(dto.getClass());
        if(V.notEmpty(annoList)){
            for(AnnoJoiner anno : annoList){
                if(V.notEmpty(anno.getJoin()) && fieldNameSet != null && fieldNameSet.contains(anno.getFieldName())){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取dto类中定义的BindQuery注解
     * @param dtoClass class
     * @return 结果
     */
    public static List<AnnoJoiner> getBindQueryAnnos(Class<?> dtoClass){
        String dtoClassName = dtoClass.getName();
        if(dtoClassBindQueryCacheMap.containsKey(dtoClassName)){
            return dtoClassBindQueryCacheMap.get(dtoClassName);
        }
        // 初始化
        List<AnnoJoiner> annos = null;
        List<Field> declaredFields = BeanUtils.extractFields(dtoClass, BindQuery.class);
        int index = 1;
        Map<String, String> joinOn2Alias = new HashMap<>();
        for (Field field : declaredFields) {
            BindQuery query = field.getAnnotation(BindQuery.class);
            if(query == null || query.ignore()){
                continue;
            }
            if(annos == null){
                annos = new ArrayList<>();
            }
            AnnoJoiner annoJoiner = new AnnoJoiner(field, query);
            // 关联对象，设置别名
            if(V.notEmpty(annoJoiner.getJoin())){
                String key = annoJoiner.getJoin() + ":" + annoJoiner.getCondition();
                String alias = joinOn2Alias.get(key);
                if(alias == null){
                    alias = "r"+index;
                    annoJoiner.setAlias(alias);
                    index++;
                    joinOn2Alias.put(key, alias);
                }
                else{
                    annoJoiner.setAlias(alias);
                }
                annoJoiner.parse();
            }
            annos.add(annoJoiner);
        }
        if(annos == null){
            annos = Collections.emptyList();
        }
        dtoClassBindQueryCacheMap.put(dtoClassName, annos);
        return annos;
    }

    /**
     * 获取注解joiner
     * @param dtoClass class
     * @param fieldNames fn
     * @return 结果
     */
    public static List<AnnoJoiner> getAnnoJoiners(Class<?> dtoClass, Collection<String> fieldNames) {
        List<AnnoJoiner> annoList = getBindQueryAnnos(dtoClass);
        // 不过滤  返回全部
        if(fieldNames == null){
            return annoList;
        }
        // 过滤
        if(V.notEmpty(annoList)){
            List<AnnoJoiner> matchedAnnoList = new ArrayList<>();
            for(AnnoJoiner anno : annoList){
                if(fieldNames.contains(anno.getFieldName())){
                    matchedAnnoList.add(anno);
                }
            }
            return matchedAnnoList;
        }
        return Collections.emptyList();
    }

    /**
     * 获取注解joiner
     * @param dtoClass dto
     * @param key key
     * @return 结果
     */
    public static AnnoJoiner getAnnoJoiner(Class<?> dtoClass, String key) {
        List<AnnoJoiner> annoList = getBindQueryAnnos(dtoClass);
        if(V.notEmpty(annoList)){
            for(AnnoJoiner anno : annoList){
                if(key.equals(anno.getFieldName())){
                    return anno;
                }
            }
        }
        return null;
    }

}
